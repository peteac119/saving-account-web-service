package org.pete.service;

import org.pete.entity.Users;
import org.pete.entity.SavingAccounts;
import org.pete.model.request.CreateSavingAccountRequest;
import org.pete.model.request.DepositRequest;
import org.pete.model.request.TransferRequest;
import org.pete.model.result.CreateSavingAccountResult;
import org.pete.model.result.DepositResult;
import org.pete.repository.UserRepository;
import org.pete.repository.SavingAccountRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@Service
public class SavingAccountService {

    private final SavingAccountRepository savingAccountRepository;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public SavingAccountService(SavingAccountRepository savingAccountRepository,
                                UserRepository userRepository,
                                BCryptPasswordEncoder bCryptPasswordEncoder) {
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

        SavingAccounts savingAccounts = createNewAccount(users, request);
        savingAccountRepository.save(savingAccounts);

        return new CreateSavingAccountResult.Success(savingAccounts.getAccountNumber(), savingAccounts.getBalance());
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

        return new DepositResult.Success(savingAccounts.getAccountNumber(), savingAccounts.getBalance());
    }

    private boolean depositAmountIsLessThanOne(BigDecimal depositAmount) {
        return Objects.isNull(depositAmount) || BigDecimal.ONE.compareTo(depositAmount) > 0;
    }

    @Transactional
    public void transfer(TransferRequest request, Long senderId) {
        // TODO Need to make sure that the user is really a sender.
        SavingAccounts senderAccount = savingAccountRepository.findOneByAccountNumber(request.getSenderAccountNum());
        SavingAccounts beneficiaryAccount = savingAccountRepository.findOneByAccountNumber(request.getBeneficiaryAccountNum());

        if (Objects.isNull(senderAccount) || Objects.isNull(beneficiaryAccount)) {
            return;
        }

        if (Objects.isNull(request.getPinNumber())) {
            return;
        }

        Users sender = senderAccount.getUsers();
        String pinNumber = request.getPinNumber();
        if (pinNumberIsNotTheSame(pinNumber, sender.getPinNum())) {
            return;
        }

        BigDecimal transferAmount = request.getTransferAmount();

        if (transferAmountIsLessThanOne(transferAmount)) {
            return;
        }

        if (senderBalanceIsLessThanTransferAmount(transferAmount, senderAccount)) {
            return;
        }

        BigDecimal newSenderBalance = senderAccount.getBalance().subtract(transferAmount);
        BigDecimal newBeneficiaryBalance = beneficiaryAccount.getBalance().add(transferAmount);

        senderAccount.setBalance(newSenderBalance);
        beneficiaryAccount.setBalance(newBeneficiaryBalance);
    }

    private boolean pinNumberIsNotTheSame(String pinNumber, String originalPinNum) {
        return originalPinNum.equals(bCryptPasswordEncoder.encode(pinNumber));
    }

    private boolean senderBalanceIsLessThanTransferAmount(BigDecimal transferAmount, SavingAccounts senderAccount) {
        return senderAccount.getBalance().compareTo(transferAmount) < 0;
    }

    private boolean transferAmountIsLessThanOne(BigDecimal transferAmount) {
        return Objects.isNull(transferAmount) || BigDecimal.ONE.compareTo(transferAmount) < 0;
    }

    @Transactional(readOnly = true)
    public void findSavingAccount(Long customerId) {
        List<SavingAccounts> savingAccountsList = savingAccountRepository.findByCustomerId(customerId);

        if (Objects.isNull(savingAccountsList) || savingAccountsList.isEmpty()) {
            return;
        }

        return;
    }
}
