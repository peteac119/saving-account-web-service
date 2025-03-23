package org.pete.service;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
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

import java.math.BigDecimal;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class SavingAccountsServiceTest {

    private final SavingAccountRepository mockSavingAccountRepository = Mockito.mock(SavingAccountRepository.class);
    private final UserRepository mockUserRepository = Mockito.mock(UserRepository.class);
    private final BCryptPasswordEncoder mockBCryptPasswordEncoder = Mockito.mock(BCryptPasswordEncoder.class);
    private final TransactionAuditLogService mockTransactionAuditLogService = Mockito.mock(TransactionAuditLogService.class);
    private final SavingAccountService savingAccountService = new SavingAccountService(
            mockSavingAccountRepository,
            mockUserRepository,
            mockTransactionAuditLogService,
            mockBCryptPasswordEncoder
    );

    @Nested
    public class CreateSavingAccountsTestSuite {
        @Test
        public void should_create_account_with_deposit_amount_successfully() {
            ArgumentCaptor<SavingAccounts> savingAccountArgumentCaptor = ArgumentCaptor.forClass(SavingAccounts.class);
            CreateSavingAccountRequest mockRequest = new CreateSavingAccountRequest(
                    "ชื่อภาษาไทย",
                    "testEnglishNameSuccess",
                    "2235485123123",
                    BigDecimal.TEN
            );
            Users mockUsers = new Users();
            Long mockAccountNumber = 1000000L;
            when(mockUserRepository.findOneByThaiNameAndEnglishNameAndCitizenId(
                    mockRequest.getThaiName(),
                    mockRequest.getEnglishName(),
                    mockRequest.getCitizenId()
            )).thenReturn(mockUsers);
            when(mockSavingAccountRepository.save(any(SavingAccounts.class))).thenAnswer(input -> input.getArguments()[0]);
            when(mockSavingAccountRepository.nextAccountNumber()).thenReturn(mockAccountNumber);

            CreateSavingAccountResult actualResult = savingAccountService.createSavingAccount(mockRequest);

            assertThat(actualResult, instanceOf(CreateSavingAccountResult.Success.class));
            CreateSavingAccountResult.Success successResult = (CreateSavingAccountResult.Success) actualResult;
            assertEquals(Long.toString(mockAccountNumber), successResult.getAccountNumber());
            assertEquals(mockRequest.getDepositAmount(), successResult.getCurrentBalance());
            verify(mockSavingAccountRepository, times(1)).save(savingAccountArgumentCaptor.capture());
            assertSavingAccountInfo(mockRequest, savingAccountArgumentCaptor.getValue(), mockAccountNumber, mockUsers);
            verify(mockTransactionAuditLogService, times(1)).logTransaction(
                    savingAccountArgumentCaptor.getValue(),
                    TransactionAction.DEPOSIT,
                    Channel.TELLER,
                    savingAccountArgumentCaptor.getValue().getBalance(),
                    mockRequest.getDepositAmount(),
                    "This account is created by Teller."
            );
        }

        @Test
        public void should_create_account_with_initial_amount_successfully() {
            ArgumentCaptor<SavingAccounts> savingAccountArgumentCaptor = ArgumentCaptor.forClass(SavingAccounts.class);
            CreateSavingAccountRequest mockRequest = new CreateSavingAccountRequest(
                    "ชื่อภาษาไทย",
                    "testEnglishNameSuccess",
                    "2235485123123",
                    null
            );
            Users mockUsers = new Users();
            Long mockAccountNumber = 1000000L;
            when(mockUserRepository.findOneByThaiNameAndEnglishNameAndCitizenId(
                    mockRequest.getThaiName(),
                    mockRequest.getEnglishName(),
                    mockRequest.getCitizenId()
            )).thenReturn(mockUsers);
            when(mockSavingAccountRepository.save(any(SavingAccounts.class))).thenAnswer(input -> input.getArguments()[0]);
            when(mockSavingAccountRepository.nextAccountNumber()).thenReturn(mockAccountNumber);

            CreateSavingAccountResult actualResult = savingAccountService.createSavingAccount(mockRequest);

            assertThat(actualResult, instanceOf(CreateSavingAccountResult.Success.class));
            CreateSavingAccountResult.Success successResult = (CreateSavingAccountResult.Success) actualResult;
            assertEquals(Long.toString(mockAccountNumber), successResult.getAccountNumber());
            assertEquals(BigDecimal.ZERO, successResult.getCurrentBalance());
            verify(mockSavingAccountRepository, times(1)).save(savingAccountArgumentCaptor.capture());
            assertSavingAccountInfo(mockRequest, savingAccountArgumentCaptor.getValue(), mockAccountNumber, mockUsers);
            verify(mockTransactionAuditLogService, times(1)).logTransaction(
                    savingAccountArgumentCaptor.getValue(),
                    TransactionAction.DEPOSIT,
                    Channel.TELLER,
                    savingAccountArgumentCaptor.getValue().getBalance(),
                    mockRequest.getDepositAmount(),
                    "This account is created by Teller."
            );
        }

        @Test
        public void should_return_amount_is_negative_if_negative_amount_is_provided() {
            Users mockUsers = new Users();
            CreateSavingAccountRequest mockRequest = new CreateSavingAccountRequest(
                    "ส่งเงินติดลบมา",
                    "testNegativeAmount",
                    "2235485176248",
                    BigDecimal.TEN.negate()
            );
            when(mockUserRepository.findOneByThaiNameAndEnglishNameAndCitizenId(
                    mockRequest.getThaiName(),
                    mockRequest.getEnglishName(),
                    mockRequest.getCitizenId()
            )).thenReturn(mockUsers);

            CreateSavingAccountResult actualResult = savingAccountService.createSavingAccount(mockRequest);

            assertThat(actualResult, instanceOf(CreateSavingAccountResult.AmountIsNegative.class));
            verify(mockSavingAccountRepository, times(0)).save(any(SavingAccounts.class));
        }

        @Test
        public void should_return_customer_not_found_if_customer_does_not_exist() {
            CreateSavingAccountRequest mockRequest = new CreateSavingAccountRequest(
                    "ชื่อภาษาไทยที่ไม่มีจริง",
                    "testInvalidEnglishName",
                    "2235485155248",
                    BigDecimal.TEN
            );
            when(mockUserRepository.findOneByThaiNameAndEnglishNameAndCitizenId(
                    mockRequest.getThaiName(),
                    mockRequest.getEnglishName(),
                    mockRequest.getCitizenId()
            )).thenReturn(null);

            CreateSavingAccountResult actualResult = savingAccountService.createSavingAccount(mockRequest);

            assertThat(actualResult, instanceOf(CreateSavingAccountResult.CustNotFound.class));
            verify(mockSavingAccountRepository, times(0)).save(any(SavingAccounts.class));
        }

        private void assertSavingAccountInfo(CreateSavingAccountRequest mockRequest,
                                             SavingAccounts savingAccounts,
                                             Long mockAccountNumber,
                                             Users mockUsers) {
            BigDecimal expectedBalance = Optional.ofNullable(mockRequest.getDepositAmount()).orElse(BigDecimal.ZERO);
            assertEquals(expectedBalance, savingAccounts.getBalance());
            assertEquals(mockAccountNumber.toString(), savingAccounts.getAccountNumber());
            assertEquals(mockUsers, savingAccounts.getUsers());
        }
    }

    @Nested
    public class DepositTestSuite {
        @Test
        public void should_deposit_with_specific_amount_successfully() {
            DepositRequest mockRequest = new DepositRequest(BigDecimal.valueOf(500.25), "2254487");
            SavingAccounts mockSavingAccounts = new SavingAccounts();
            mockSavingAccounts.setAccountNumber(mockRequest.getAccountNumber());
            mockSavingAccounts.setBalance(BigDecimal.TEN);
            when(mockSavingAccountRepository.findOneByAccountNumber(mockRequest.getAccountNumber())).thenReturn(mockSavingAccounts);

            DepositResult actualResult = savingAccountService.deposit(mockRequest);

            assertThat(actualResult, instanceOf(DepositResult.Success.class));
            DepositResult.Success successResult = (DepositResult.Success) actualResult;
            assertEquals(mockRequest.getAccountNumber(), successResult.getAccountNumber());
            assertEquals(mockSavingAccounts.getBalance(), successResult.getNewBalance());
            verify(mockTransactionAuditLogService, times(1)).logTransaction(
                    mockSavingAccounts,
                    TransactionAction.DEPOSIT,
                    Channel.TELLER,
                    mockSavingAccounts.getBalance(),
                    mockRequest.getDepositAmount(),
                    "Deposit via Teller"
            );
        }

        @Test
        public void should_not_deposit_if_the_saving_account_is_not_found() {
            DepositRequest mockRequest = new DepositRequest(BigDecimal.valueOf(200.00), "2254888");
            when(mockSavingAccountRepository.findOneByAccountNumber(mockRequest.getAccountNumber())).thenReturn(null);

            DepositResult actualResult = savingAccountService.deposit(mockRequest);

            assertThat(actualResult, instanceOf(DepositResult.SavingAccountNotFound.class));
        }

        @Test
        public void should_not_deposit_if_the_deposit_amount_is_less_than_one() {
            DepositRequest mockRequest = new DepositRequest(BigDecimal.ZERO, "5524867");
            SavingAccounts mockSavingAccounts = new SavingAccounts();
            mockSavingAccounts.setAccountNumber(mockRequest.getAccountNumber());
            mockSavingAccounts.setBalance(BigDecimal.ONE);
            when(mockSavingAccountRepository.findOneByAccountNumber(mockRequest.getAccountNumber())).thenReturn(mockSavingAccounts);

            DepositResult actualResult = savingAccountService.deposit(mockRequest);

            assertThat(actualResult, instanceOf(DepositResult.DepositAmountIsLessThanOne.class));
        }
    }

    @Nested
    public class TransferTestSuite {
        @Test
        public void should_transfer_money_successfully() {
            String mockPinNumber = "223485";
            Long mockUserId = 1L;
            BigDecimal originalSenderBalance = BigDecimal.valueOf(500L);
            BigDecimal originalBeneficiaryBalance = BigDecimal.valueOf(600L);
            SavingAccounts mockSenderAccount = mockSavingAccount("5584756", BigDecimal.valueOf(500L), mockPinNumber, mockUserId);
            SavingAccounts mockBeneficiaryAccount = mockSavingAccount("4498754", BigDecimal.valueOf(600L), "558472", 2L);
            BigDecimal mockTransferAmount = BigDecimal.TEN;
            TransferRequest mockRequest = new TransferRequest(
                    mockSenderAccount.getAccountNumber(),
                    mockBeneficiaryAccount.getAccountNumber(),
                    mockTransferAmount,
                    mockPinNumber
            );
            when(mockSavingAccountRepository.findOneByAccountNumber(mockSenderAccount.getAccountNumber())).thenReturn(mockSenderAccount);
            when(mockSavingAccountRepository.findOneByAccountNumber(mockBeneficiaryAccount.getAccountNumber())).thenReturn(mockBeneficiaryAccount);
            when(mockBCryptPasswordEncoder.matches(mockPinNumber, mockSenderAccount.getUsers().getPinNum())).thenReturn(true);

            TransferResult actualResult = savingAccountService.transfer(mockRequest, mockUserId);

            assertThat(actualResult, instanceOf(TransferResult.Success.class));
            TransferResult.Success successResult = (TransferResult.Success) actualResult;
            assertEquals(mockSenderAccount.getAccountNumber(), successResult.getSenderAccountNumber());
            assertEquals(mockBeneficiaryAccount.getAccountNumber(), successResult.getBeneficiaryAccountNumber());
            assertEquals(originalSenderBalance.subtract(mockTransferAmount), successResult.getCurrentSenderBalance());
            assertEquals(originalBeneficiaryBalance.add(mockTransferAmount), successResult.getCurrentBeneficiaryBalance());
            verify(mockTransactionAuditLogService, times(1)).logTransaction(
                    mockSenderAccount,
                    TransactionAction.TRANSFER,
                    Channel.CUSTOMER,
                    mockSenderAccount.getBalance(),
                    mockTransferAmount,
                    "Transfer to " + mockBeneficiaryAccount.getAccountNumber()
            );
            verify(mockTransactionAuditLogService, times(1)).logTransaction(
                    mockBeneficiaryAccount,
                    TransactionAction.DEPOSIT,
                    Channel.CUSTOMER,
                    mockBeneficiaryAccount.getBalance(),
                    mockTransferAmount,
                    "Receive transfer from " + mockSenderAccount.getAccountNumber()
            );
        }

        @Test
        public void should_not_transfer_money_if_sender_account_does_not_exist() {
            String mockInvalidAccountNumber = "225487";
            SavingAccounts mockBeneficiaryAccount = mockSavingAccount("2548687", BigDecimal.valueOf(600L), "662144", 3L);
            BigDecimal mockTransferAmount = BigDecimal.TEN;
            TransferRequest mockRequest = new TransferRequest(
                    mockInvalidAccountNumber,
                    mockBeneficiaryAccount.getAccountNumber(),
                    mockTransferAmount,
                    "225486"
            );
            when(mockSavingAccountRepository.findOneByAccountNumber(mockInvalidAccountNumber)).thenReturn(null);
            when(mockSavingAccountRepository.findOneByAccountNumber(mockBeneficiaryAccount.getAccountNumber())).thenReturn(mockBeneficiaryAccount);

            TransferResult actualResult = savingAccountService.transfer(mockRequest, 5L);

            assertThat(actualResult, instanceOf(TransferResult.SavingAccountNotFound.class));
            TransferResult.SavingAccountNotFound savingAccountNotFound = (TransferResult.SavingAccountNotFound) actualResult;
            assertEquals("Either sender or beneficiary account is incorrect.", savingAccountNotFound.getMessage());
        }

        @Test
        public void should_not_transfer_money_if_both_sender_and_beneficiary_accounts_are_the_same() {
            SavingAccounts mockSenderAccount = mockSavingAccount("4455667", BigDecimal.valueOf(500L), "662144", 3L);
            SavingAccounts mockBeneficiaryAccount = mockSavingAccount("4455667", BigDecimal.valueOf(600L), "662144", 3L);
            BigDecimal mockTransferAmount = BigDecimal.TEN;
            TransferRequest mockRequest = new TransferRequest(
                    mockSenderAccount.getAccountNumber(),
                    mockBeneficiaryAccount.getAccountNumber(),
                    mockTransferAmount,
                    "662144"
            );
            when(mockSavingAccountRepository.findOneByAccountNumber(mockSenderAccount.getAccountNumber())).thenReturn(mockSenderAccount);
            when(mockSavingAccountRepository.findOneByAccountNumber(mockBeneficiaryAccount.getAccountNumber())).thenReturn(mockBeneficiaryAccount);

            TransferResult actualResult = savingAccountService.transfer(mockRequest, 5L);

            assertThat(actualResult, instanceOf(TransferResult.SameAccountNumber.class));
        }

        @Test
        public void should_not_transfer_money_if_beneficiary_account_does_not_exist() {
            String mockInvalidAccountNumber = "8854789";
            SavingAccounts mockSenderAccount = mockSavingAccount("8847598", BigDecimal.valueOf(400L), "996875", 7L);
            BigDecimal mockTransferAmount = BigDecimal.TEN;
            TransferRequest mockRequest = new TransferRequest(
                    mockSenderAccount.getAccountNumber(),
                    mockInvalidAccountNumber,
                    mockTransferAmount,
                    "996875"
            );
            when(mockSavingAccountRepository.findOneByAccountNumber(mockSenderAccount.getAccountNumber())).thenReturn(mockSenderAccount);
            when(mockSavingAccountRepository.findOneByAccountNumber(mockInvalidAccountNumber)).thenReturn(null);

            TransferResult actualResult = savingAccountService.transfer(mockRequest, 5L);

            assertThat(actualResult, instanceOf(TransferResult.SavingAccountNotFound.class));
            TransferResult.SavingAccountNotFound savingAccountNotFound = (TransferResult.SavingAccountNotFound) actualResult;
            assertEquals("Either sender or beneficiary account is incorrect.", savingAccountNotFound.getMessage());
        }

        @Test
        public void should_not_transfer_money_if_users_do_not_provide_pin_number() {
            SavingAccounts mockSenderAccount = mockSavingAccount("452878", BigDecimal.valueOf(400L), "965875", 10L);
            SavingAccounts mockBeneficiaryAccount = mockSavingAccount("4458622", BigDecimal.valueOf(600L), "220125", 20L);
            BigDecimal mockTransferAmount = BigDecimal.TEN;
            TransferRequest mockRequest = new TransferRequest(
                    mockSenderAccount.getAccountNumber(),
                    mockBeneficiaryAccount.getAccountNumber(),
                    mockTransferAmount,
                    null
            );
            when(mockSavingAccountRepository.findOneByAccountNumber(mockSenderAccount.getAccountNumber())).thenReturn(mockSenderAccount);
            when(mockSavingAccountRepository.findOneByAccountNumber(mockBeneficiaryAccount.getAccountNumber())).thenReturn(mockBeneficiaryAccount);

            TransferResult actualResult = savingAccountService.transfer(mockRequest, 10L);

            assertThat(actualResult, instanceOf(TransferResult.NotPinNumberProvided.class));
        }

        @Test
        public void should_not_transfer_money_if_pin_number_is_wrong() {
            String wrongPinNumber = "551879";
            SavingAccounts mockSenderAccount = mockSavingAccount("2245879", BigDecimal.valueOf(400L), "455879", 50L);
            SavingAccounts mockBeneficiaryAccount = mockSavingAccount("6698999", BigDecimal.valueOf(600L), "550001", 30L);
            BigDecimal mockTransferAmount = BigDecimal.TEN;
            TransferRequest mockRequest = new TransferRequest(
                    mockSenderAccount.getAccountNumber(),
                    mockBeneficiaryAccount.getAccountNumber(),
                    mockTransferAmount,
                    wrongPinNumber
            );
            when(mockSavingAccountRepository.findOneByAccountNumber(mockSenderAccount.getAccountNumber())).thenReturn(mockSenderAccount);
            when(mockSavingAccountRepository.findOneByAccountNumber(mockBeneficiaryAccount.getAccountNumber())).thenReturn(mockBeneficiaryAccount);
            when(mockBCryptPasswordEncoder.matches(wrongPinNumber, mockSenderAccount.getUsers().getPinNum())).thenReturn(false);

            TransferResult actualResult = savingAccountService.transfer(mockRequest, 50L);

            assertThat(actualResult, instanceOf(TransferResult.WrongPinNumber.class));
        }

        @Test
        public void should_not_transfer_money_if_user_id_of_sender_is_not_the_same_in_sender_account() {
            Long wrongUserId = 500L;
            String mockPinNumber = "455879";
            SavingAccounts mockSenderAccount = mockSavingAccount("2201478", BigDecimal.valueOf(400L), mockPinNumber, 60L);
            SavingAccounts mockBeneficiaryAccount = mockSavingAccount("4425689", BigDecimal.valueOf(600L), "550001", 40L);
            BigDecimal mockTransferAmount = BigDecimal.TEN;
            TransferRequest mockRequest = new TransferRequest(
                    mockSenderAccount.getAccountNumber(),
                    mockBeneficiaryAccount.getAccountNumber(),
                    mockTransferAmount,
                    mockPinNumber
            );
            when(mockSavingAccountRepository.findOneByAccountNumber(mockSenderAccount.getAccountNumber())).thenReturn(mockSenderAccount);
            when(mockSavingAccountRepository.findOneByAccountNumber(mockBeneficiaryAccount.getAccountNumber())).thenReturn(mockBeneficiaryAccount);

            TransferResult actualResult = savingAccountService.transfer(mockRequest, wrongUserId);

            assertThat(actualResult, instanceOf(TransferResult.WrongSenderAccount.class));
        }

        @Test
        public void should_not_transfer_money_if_sender_does_not_have_enough_amount() {
            Long mockUserId = 40L;
            String mockPinNumber = "455879";
            SavingAccounts mockSenderAccount = mockSavingAccount("6667789", BigDecimal.ZERO, mockPinNumber, mockUserId);
            SavingAccounts mockBeneficiaryAccount = mockSavingAccount("7775523", BigDecimal.valueOf(600L), "445879", 20L);
            BigDecimal mockTransferAmount = BigDecimal.TEN;
            TransferRequest mockRequest = new TransferRequest(
                    mockSenderAccount.getAccountNumber(),
                    mockBeneficiaryAccount.getAccountNumber(),
                    mockTransferAmount,
                    mockPinNumber
            );
            when(mockSavingAccountRepository.findOneByAccountNumber(mockSenderAccount.getAccountNumber())).thenReturn(mockSenderAccount);
            when(mockSavingAccountRepository.findOneByAccountNumber(mockBeneficiaryAccount.getAccountNumber())).thenReturn(mockBeneficiaryAccount);
            when(mockBCryptPasswordEncoder.matches(mockPinNumber, mockSenderAccount.getUsers().getPinNum())).thenReturn(true);

            TransferResult actualResult = savingAccountService.transfer(mockRequest, mockUserId);

            assertThat(actualResult, instanceOf(TransferResult.NotEnoughBalance.class));
            TransferResult.NotEnoughBalance notEnoughBalance = (TransferResult.NotEnoughBalance) actualResult;
            assertEquals(mockSenderAccount.getAccountNumber(), notEnoughBalance.getSenderAccountNumber());
            assertEquals(mockSenderAccount.getBalance(), notEnoughBalance.getCurrentSenderBalance());
        }

        @Test
        public void should_not_transfer_money_if_transfer_amount_is_less_than_one() {
            Long mockUserId = 40L;
            String mockPinNumber = "455879";
            SavingAccounts mockSenderAccount = mockSavingAccount("8889942", BigDecimal.valueOf(500L), mockPinNumber, mockUserId);
            SavingAccounts mockBeneficiaryAccount = mockSavingAccount("0021548", BigDecimal.valueOf(600L), "445879", 20L);
            BigDecimal mockTransferAmount = BigDecimal.ZERO;
            TransferRequest mockRequest = new TransferRequest(
                    mockSenderAccount.getAccountNumber(),
                    mockBeneficiaryAccount.getAccountNumber(),
                    mockTransferAmount,
                    mockPinNumber
            );
            when(mockSavingAccountRepository.findOneByAccountNumber(mockSenderAccount.getAccountNumber())).thenReturn(mockSenderAccount);
            when(mockSavingAccountRepository.findOneByAccountNumber(mockBeneficiaryAccount.getAccountNumber())).thenReturn(mockBeneficiaryAccount);
            when(mockBCryptPasswordEncoder.matches(mockPinNumber, mockSenderAccount.getUsers().getPinNum())).thenReturn(true);

            TransferResult actualResult = savingAccountService.transfer(mockRequest, mockUserId);

            assertThat(actualResult, instanceOf(TransferResult.TransferAmountIsLessThanOne.class));
        }

    }

    @Nested
    public class FindSavingAccountInfoTestSuite {
        @Test
        public void should_get_account_info_successfully() {
            String mockAccountNumber = "accountNumber";
            Long mockRequesterId = 5L;
            SavingAccounts mockSavingAccount = mockSavingAccount(mockAccountNumber, BigDecimal.TEN, "testPinNumber", mockRequesterId);
            when(mockSavingAccountRepository.findOneByAccountNumber(mockAccountNumber)).thenReturn(mockSavingAccount);

            FindSavingAccountInfoResult actualResult = savingAccountService.findSavingAccountInfo(mockAccountNumber, mockRequesterId);

            assertThat(actualResult, instanceOf(FindSavingAccountInfoResult.Success.class));
            FindSavingAccountInfoResult.Success successResult = (FindSavingAccountInfoResult.Success) actualResult;
            assertEquals(mockAccountNumber, successResult.getAccountNumber());
            assertEquals(BigDecimal.TEN, successResult.getCurrentBalance());
        }

        @Test
        public void should_return_account_not_found_if_account_is_not_found_in_DB() {
            String mockAccountNumber = "invalidAccountNumber";
            Long mockRequesterId = 5L;
            when(mockSavingAccountRepository.findOneByAccountNumber(mockAccountNumber)).thenReturn(null);

            FindSavingAccountInfoResult actualResult = savingAccountService.findSavingAccountInfo(mockAccountNumber, mockRequesterId);

            assertThat(actualResult, instanceOf(FindSavingAccountInfoResult.AccountNotFound.class));
        }

        @Test
        public void should_return_wrong_account_number_if_requester_id_is_not_the_same_in_the_saving_account() {
            String mockAccountNumber = "someAccountNumber";
            Long mockRequesterId = 10L;
            SavingAccounts mockSavingAccount = mockSavingAccount(mockAccountNumber, BigDecimal.TEN, "testPinNumber", 5L);
            when(mockSavingAccountRepository.findOneByAccountNumber(mockAccountNumber)).thenReturn(mockSavingAccount);

            FindSavingAccountInfoResult actualResult = savingAccountService.findSavingAccountInfo(mockAccountNumber, mockRequesterId);

            assertThat(actualResult, instanceOf(FindSavingAccountInfoResult.WrongAccountNumber.class));
        }
    }

    private SavingAccounts mockSavingAccount(String accountNumber,
                                             BigDecimal balance,
                                             String pinNumber,
                                             Long userId) {
        Users users = new Users();
        users.setId(userId);
        users.setPinNum(pinNumber);

        SavingAccounts savingAccounts = new SavingAccounts();
        savingAccounts.setAccountNumber(accountNumber);
        savingAccounts.setBalance(balance);
        savingAccounts.setUsers(users);

        return savingAccounts;
    }
}