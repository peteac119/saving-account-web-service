package org.pete.service;

import org.pete.entity.Customer;
import org.pete.entity.SavingAccount;
import org.pete.model.request.CreateSavingAccountRequest;
import org.pete.model.request.DepositRequest;
import org.pete.model.request.TransferRequest;
import org.pete.model.result.CreateSavingAccountResult;
import org.pete.model.result.DepositResult;
import org.pete.repository.CustomerRepository;
import org.pete.repository.SavingAccountRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class SavingAccountService {

    private final SavingAccountRepository savingAccountRepository;
    private final CustomerRepository customerRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public SavingAccountService(SavingAccountRepository savingAccountRepository,
                                CustomerRepository customerRepository,
                                BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.savingAccountRepository = savingAccountRepository;
        this.customerRepository = customerRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    @Transactional
    public CreateSavingAccountResult createSavingAccount(CreateSavingAccountRequest request) {
        Customer customer = customerRepository.findOneByThaiNameAndEnglishNameAndCitizenId(
                request.getThaiName(),
                request.getEnglishName(),
                request.getCitizenId()
        );

        if (Objects.isNull(customer)) {
            return new CreateSavingAccountResult.CustNotFound();
        }

        SavingAccount savingAccount = createNewAccount(customer, request);
        savingAccountRepository.save(savingAccount);

        return new CreateSavingAccountResult.Success(savingAccount.getAccountNumber(), savingAccount.getBalance());
    }

    private SavingAccount createNewAccount(Customer customer, CreateSavingAccountRequest request) {
        Long newAccountNumber = savingAccountRepository.nextAccountNumber();
        BigDecimal depositAmount = request.getDepositAmount();

        SavingAccount savingAccount = new SavingAccount();
        savingAccount.setAccountNumber(generateAccountNumber(newAccountNumber));
        savingAccount.setCustomer(customer);

        if (Objects.nonNull(depositAmount)) {
            savingAccount.setBalance(depositAmount);
        } else {
            savingAccount.setBalance(BigDecimal.ZERO);
        }

        return savingAccount;
    }

    private String generateAccountNumber(Long newId) {
        return String.format("%07d", newId);
    }

    @Transactional
    public DepositResult deposit(DepositRequest request) {
        BigDecimal depositAmount = request.getDepositAmount();

        if (depositAmountIsLessThanOne(depositAmount)) {
            return new DepositResult.DepositAmountIsLessThanOne();
        }

        SavingAccount savingAccount = savingAccountRepository.findOneByAccountNumber(request.getAccountNumber());

        if (Objects.isNull(savingAccount)) {
            return new DepositResult.SavingAccountNotFound();
        }

        BigDecimal currentBalance = savingAccount.getBalance();
        savingAccount.setBalance(currentBalance.add(depositAmount));

        return new DepositResult.Success(savingAccount.getAccountNumber(), savingAccount.getBalance());
    }

    private boolean depositAmountIsLessThanOne(BigDecimal depositAmount) {
        return Objects.isNull(depositAmount) || BigDecimal.ONE.compareTo(depositAmount) > 0;
    }

    @Transactional
    public void transfer(TransferRequest request, Long senderId) {
        // TODO Need to make sure that the user is really a sender.
        SavingAccount senderAccount = savingAccountRepository.findOneByAccountNumber(request.getSenderAccountNum());
        SavingAccount beneficiaryAccount = savingAccountRepository.findOneByAccountNumber(request.getBeneficiaryAccountNum());

        if (Objects.isNull(senderAccount) || Objects.isNull(beneficiaryAccount)) {
            return;
        }

        if (Objects.isNull(request.getPinNumber())) {
            return;
        }

        Customer sender = senderAccount.getCustomer();
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

    private boolean senderBalanceIsLessThanTransferAmount(BigDecimal transferAmount, SavingAccount senderAccount) {
        return senderAccount.getBalance().compareTo(transferAmount) < 0;
    }

    private boolean transferAmountIsLessThanOne(BigDecimal transferAmount) {
        return Objects.isNull(transferAmount) || BigDecimal.ONE.compareTo(transferAmount) < 0;
    }

    @Transactional(readOnly = true)
    public void findSavingAccount(Long customerId) {
        List<SavingAccount> savingAccountList = savingAccountRepository.findByCustomerId(customerId);

        if (Objects.isNull(savingAccountList) || savingAccountList.isEmpty()) {
            return;
        }

        return;
    }
}
