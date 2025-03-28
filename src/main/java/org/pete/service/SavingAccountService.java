package org.pete.service;

import org.pete.constant.Channel;
import org.pete.constant.TransactionAction;
import org.pete.entity.Users;
import org.pete.entity.SavingAccounts;
import org.pete.model.request.CreateSavingAccountRequest;
import org.pete.model.request.DepositRequest;
import org.pete.model.request.TransferRequest;
import org.pete.model.result.CreateSavingAccountResult;
import org.pete.model.result.DepositResult;
import org.pete.model.result.FindSavingAccountInfoResult;
import org.pete.model.result.TransferResult;
import org.pete.repository.UserRepository;
import org.pete.repository.SavingAccountRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Objects;

@Service
public class SavingAccountService {

    private final SavingAccountRepository savingAccountRepository;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final TransactionAuditLogService transactionAuditLogService;

    public SavingAccountService(SavingAccountRepository savingAccountRepository,
                                UserRepository userRepository,
                                TransactionAuditLogService transactionAuditLogService,
                                BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.transactionAuditLogService = transactionAuditLogService;
        this.savingAccountRepository = savingAccountRepository;
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    @Transactional
    public CreateSavingAccountResult createSavingAccount(CreateSavingAccountRequest request) {
        Users users = userRepository.findOneByThaiNameAndEnglishNameAndCitizenId(
                request.getThaiName(),
                request.getEnglishName(),
                request.getCitizenId()
        );

        if (Objects.isNull(users)) {
            return new CreateSavingAccountResult.CustNotFound();
        }

        BigDecimal depositAmount = request.getDepositAmount();
        if (amountIsNegative(depositAmount)) {
            return new CreateSavingAccountResult.AmountIsNegative();
        }

        SavingAccounts savingAccounts = createNewAccount(users, request);
        savingAccountRepository.save(savingAccounts);

        transactionAuditLogService.logTransaction(
                savingAccounts,
                TransactionAction.DEPOSIT,
                Channel.TELLER,
                savingAccounts.getBalance(),
                request.getDepositAmount(),
                "This account is created by Teller.");

        return new CreateSavingAccountResult.Success(savingAccounts.getAccountNumber(), savingAccounts.getBalance());
    }

    private boolean amountIsNegative(BigDecimal depositAmount) {
        return Objects.nonNull(depositAmount) && depositAmount.compareTo(BigDecimal.ZERO) < 0;
    }

    private SavingAccounts createNewAccount(Users users, CreateSavingAccountRequest request) {
        Long newAccountNumber = savingAccountRepository.nextAccountNumber();
        BigDecimal depositAmount = request.getDepositAmount();

        SavingAccounts savingAccounts = new SavingAccounts();
        savingAccounts.setAccountNumber(generateAccountNumber(newAccountNumber));
        savingAccounts.setUsers(users);

        if (Objects.nonNull(depositAmount)) {
            savingAccounts.setBalance(depositAmount);
        } else {
            savingAccounts.setBalance(BigDecimal.ZERO);
        }

        return savingAccounts;
    }

    private String generateAccountNumber(Long newId) {
        return String.format("%07d", newId);
    }

    @Transactional
    public DepositResult deposit(DepositRequest request) {
        SavingAccounts savingAccounts = savingAccountRepository.findOneByAccountNumber(request.getAccountNumber());

        if (Objects.isNull(savingAccounts)) {
            return new DepositResult.SavingAccountNotFound();
        }

        BigDecimal depositAmount = request.getDepositAmount();

        if (depositAmountIsLessThanOne(depositAmount)) {
            return new DepositResult.DepositAmountIsLessThanOne();
        }

        BigDecimal currentBalance = savingAccounts.getBalance();
        savingAccounts.setBalance(currentBalance.add(depositAmount));

        transactionAuditLogService.logTransaction(
                savingAccounts,
                TransactionAction.DEPOSIT,
                Channel.TELLER,
                savingAccounts.getBalance(),
                request.getDepositAmount(),
                "Deposit via Teller");

        return new DepositResult.Success(savingAccounts.getAccountNumber(), savingAccounts.getBalance());
    }

    private boolean depositAmountIsLessThanOne(BigDecimal depositAmount) {
        return Objects.isNull(depositAmount) || BigDecimal.ONE.compareTo(depositAmount) > 0;
    }

    @Transactional
    public TransferResult transfer(TransferRequest request, Long senderId) {
        SavingAccounts senderAccount = savingAccountRepository.findOneByAccountNumber(request.getSenderAccountNum());
        SavingAccounts beneficiaryAccount = savingAccountRepository.findOneByAccountNumber(request.getBeneficiaryAccountNum());

        TransferResult validationResult = validateAccountAndAmount(senderAccount, beneficiaryAccount, request, senderId);

        if(Objects.nonNull(validationResult)) {
            return validationResult;
        }

        BigDecimal transferAmount = request.getTransferAmount();
        BigDecimal newSenderBalance = senderAccount.getBalance().subtract(transferAmount);
        BigDecimal newBeneficiaryBalance = beneficiaryAccount.getBalance().add(transferAmount);

        senderAccount.setBalance(newSenderBalance);
        beneficiaryAccount.setBalance(newBeneficiaryBalance);

        transactionAuditLogService.logTransaction(
                senderAccount,
                TransactionAction.TRANSFER,
                Channel.CUSTOMER,
                senderAccount.getBalance(),
                transferAmount,
                "Transfer to " + beneficiaryAccount.getAccountNumber());

        transactionAuditLogService.logTransaction(
                beneficiaryAccount,
                TransactionAction.DEPOSIT,
                Channel.CUSTOMER,
                beneficiaryAccount.getBalance(),
                transferAmount,
                "Receive transfer from " + senderAccount.getAccountNumber());

        return new TransferResult.Success(
                senderAccount.getAccountNumber(),
                beneficiaryAccount.getAccountNumber(),
                senderAccount.getBalance(),
                beneficiaryAccount.getBalance()
        );
    }

    private TransferResult validateAccountAndAmount(SavingAccounts senderAccount,
                                                    SavingAccounts beneficiaryAccount,
                                                    TransferRequest request,
                                                    Long senderId) {
        if (Objects.isNull(senderAccount) || Objects.isNull(beneficiaryAccount)) {
            return new TransferResult.SavingAccountNotFound("Either sender or beneficiary account is incorrect.");
        }

        if (Objects.equals(senderAccount.getAccountNumber(), beneficiaryAccount.getAccountNumber())) {
            return new TransferResult.SameAccountNumber();
        }

        if (Objects.isNull(request.getPinNumber())) {
            return new TransferResult.NotPinNumberProvided();
        }

        Users sender = senderAccount.getUsers();

        if (!Objects.equals(sender.getId(), senderId)) {
            return new TransferResult.WrongSenderAccount();
        }

        if (pinNumberIsNotTheSame(request.getPinNumber(), sender.getPinNum())) {
            return new TransferResult.WrongPinNumber();
        }

        BigDecimal transferAmount = request.getTransferAmount();

        if (transferAmountIsLessThanOne(transferAmount)) {
            return new TransferResult.TransferAmountIsLessThanOne();
        }

        if (senderBalanceIsLessThanTransferAmount(transferAmount, senderAccount)) {
            return new TransferResult.NotEnoughBalance(senderAccount.getAccountNumber(), senderAccount.getBalance());
        }

        return null;
    }

    private boolean pinNumberIsNotTheSame(String pinNumber, String originalPinNum) {
        return !bCryptPasswordEncoder.matches(pinNumber, originalPinNum);
    }

    private boolean senderBalanceIsLessThanTransferAmount(BigDecimal transferAmount, SavingAccounts senderAccount) {
        return senderAccount.getBalance().compareTo(transferAmount) < 0;
    }

    private boolean transferAmountIsLessThanOne(BigDecimal transferAmount) {
        return Objects.isNull(transferAmount) || BigDecimal.ONE.compareTo(transferAmount) > 0;
    }

    @Transactional(readOnly = true)
    public FindSavingAccountInfoResult findSavingAccountInfo(String accountNumber, Long requesterId) {
        SavingAccounts savingAccounts = savingAccountRepository.findOneByAccountNumber(accountNumber);

        if (Objects.isNull(savingAccounts)) {
            return new FindSavingAccountInfoResult.AccountNotFound();
        }

        Users accountOwner = savingAccounts.getUsers();

        if (!Objects.equals(accountOwner.getId(), requesterId)) {
            return new FindSavingAccountInfoResult.WrongAccountNumber();
        }

        return new FindSavingAccountInfoResult.Success(
                savingAccounts.getAccountNumber(),
                savingAccounts.getBalance(),
                savingAccounts.getCreationDate(),
                savingAccounts.getLastUpdateDate()
        );
    }
}
