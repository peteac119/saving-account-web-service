package org.pete.service;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.pete.entity.Users;
import org.pete.entity.SavingAccounts;
import org.pete.model.request.CreateSavingAccountRequest;
import org.pete.model.request.DepositRequest;
import org.pete.model.result.CreateSavingAccountResult;
import org.pete.model.result.DepositResult;
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
    private final SavingAccountService savingAccountService = new SavingAccountService(
            mockSavingAccountRepository,
            mockUserRepository,
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
        }

        @Test
        public void should_return_customer_not_found_if_customer_does_not_exist() {
            CreateSavingAccountRequest mockRequest = new CreateSavingAccountRequest(
                    "ชื่อภาษาไทยที่ไม่มีจริง",
                    "testInvalidEnglishName",
                    "2235485155248",
                    BigDecimal.TEN
            );;
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

    }

}