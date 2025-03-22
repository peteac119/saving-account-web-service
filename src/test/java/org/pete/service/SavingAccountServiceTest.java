package org.pete.service;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.pete.entity.Customer;
import org.pete.entity.SavingAccount;
import org.pete.model.request.CreateSavingAccountRequest;
import org.pete.model.request.DepositRequest;
import org.pete.model.result.CreateSavingAccountResult;
import org.pete.model.result.DepositResult;
import org.pete.repository.CustomerRepository;
import org.pete.repository.SavingAccountRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.math.BigDecimal;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class SavingAccountServiceTest {

    private final SavingAccountRepository mockSavingAccountRepository = Mockito.mock(SavingAccountRepository.class);
    private final CustomerRepository mockCustomerRepository = Mockito.mock(CustomerRepository.class);
    private final BCryptPasswordEncoder mockBCryptPasswordEncoder = Mockito.mock(BCryptPasswordEncoder.class);
    private final SavingAccountService savingAccountService = new SavingAccountService(
            mockSavingAccountRepository,
            mockCustomerRepository,
            mockBCryptPasswordEncoder
    );

    @Nested
    public class CreateSavingAccountTestSuite {
        @Test
        public void should_create_account_with_deposit_amount_successfully() {
            ArgumentCaptor<SavingAccount> savingAccountArgumentCaptor = ArgumentCaptor.forClass(SavingAccount.class);
            CreateSavingAccountRequest mockRequest = new CreateSavingAccountRequest(
                    "ชื่อภาษาไทย",
                    "testEnglishNameSuccess",
                    "2235485123123",
                    BigDecimal.TEN
            );
            Customer mockCustomer = new Customer();
            Long mockAccountNumber = 1000000L;
            when(mockCustomerRepository.findOneByThaiNameAndEnglishNameAndCitizenId(
                    mockRequest.getThaiName(),
                    mockRequest.getEnglishName(),
                    mockRequest.getCitizenId()
            )).thenReturn(mockCustomer);
            when(mockSavingAccountRepository.save(any(SavingAccount.class))).thenAnswer(input -> input.getArguments()[0]);
            when(mockSavingAccountRepository.nextAccountNumber()).thenReturn(mockAccountNumber);

            CreateSavingAccountResult actualResult = savingAccountService.createSavingAccount(mockRequest);

            assertThat(actualResult, instanceOf(CreateSavingAccountResult.Success.class));
            CreateSavingAccountResult.Success successResult = (CreateSavingAccountResult.Success) actualResult;
            assertEquals(Long.toString(mockAccountNumber), successResult.getAccountNumber());
            assertEquals(mockRequest.getDepositAmount(), successResult.getCurrentBalance());
            verify(mockSavingAccountRepository, times(1)).save(savingAccountArgumentCaptor.capture());
            assertSavingAccountInfo(mockRequest, savingAccountArgumentCaptor.getValue(), mockAccountNumber, mockCustomer);
        }

        @Test
        public void should_create_account_with_initial_amount_successfully() {
            ArgumentCaptor<SavingAccount> savingAccountArgumentCaptor = ArgumentCaptor.forClass(SavingAccount.class);
            CreateSavingAccountRequest mockRequest = new CreateSavingAccountRequest(
                    "ชื่อภาษาไทย",
                    "testEnglishNameSuccess",
                    "2235485123123",
                    null
            );
            Customer mockCustomer = new Customer();
            Long mockAccountNumber = 1000000L;
            when(mockCustomerRepository.findOneByThaiNameAndEnglishNameAndCitizenId(
                    mockRequest.getThaiName(),
                    mockRequest.getEnglishName(),
                    mockRequest.getCitizenId()
            )).thenReturn(mockCustomer);
            when(mockSavingAccountRepository.save(any(SavingAccount.class))).thenAnswer(input -> input.getArguments()[0]);
            when(mockSavingAccountRepository.nextAccountNumber()).thenReturn(mockAccountNumber);

            CreateSavingAccountResult actualResult = savingAccountService.createSavingAccount(mockRequest);

            assertThat(actualResult, instanceOf(CreateSavingAccountResult.Success.class));
            CreateSavingAccountResult.Success successResult = (CreateSavingAccountResult.Success) actualResult;
            assertEquals(Long.toString(mockAccountNumber), successResult.getAccountNumber());
            assertEquals(BigDecimal.ZERO, successResult.getCurrentBalance());
            verify(mockSavingAccountRepository, times(1)).save(savingAccountArgumentCaptor.capture());
            assertSavingAccountInfo(mockRequest, savingAccountArgumentCaptor.getValue(), mockAccountNumber, mockCustomer);
        }

        @Test
        public void should_return_customer_not_found_if_customer_does_not_exist() {
            CreateSavingAccountRequest mockRequest = new CreateSavingAccountRequest(
                    "ชื่อภาษาไทยที่ไม่มีจริง",
                    "testInvalidEnglishName",
                    "2235485155248",
                    BigDecimal.TEN
            );;
            when(mockCustomerRepository.findOneByThaiNameAndEnglishNameAndCitizenId(
                    mockRequest.getThaiName(),
                    mockRequest.getEnglishName(),
                    mockRequest.getCitizenId()
            )).thenReturn(null);

            CreateSavingAccountResult actualResult = savingAccountService.createSavingAccount(mockRequest);

            assertThat(actualResult, instanceOf(CreateSavingAccountResult.CustNotFound.class));
            verify(mockSavingAccountRepository, times(0)).save(any(SavingAccount.class));
        }

        private void assertSavingAccountInfo(CreateSavingAccountRequest mockRequest,
                                             SavingAccount savingAccount,
                                             Long mockAccountNumber,
                                             Customer mockCustomer) {
            BigDecimal expectedBalance = Optional.ofNullable(mockRequest.getDepositAmount()).orElse(BigDecimal.ZERO);
            assertEquals(expectedBalance, savingAccount.getBalance());
            assertEquals(mockAccountNumber.toString(), savingAccount.getAccountNumber());
            assertEquals(mockCustomer, savingAccount.getCustomer());
        }
    }

    @Nested
    public class DepositTestSuite {
        @Test
        public void should_deposit_with_specific_amount_successfully() {
            DepositRequest mockRequest = new DepositRequest(BigDecimal.valueOf(500.25), "2254487");
            SavingAccount mockSavingAccount = new SavingAccount();
            mockSavingAccount.setAccountNumber(mockRequest.getAccountNumber());
            mockSavingAccount.setBalance(BigDecimal.TEN);
            when(mockSavingAccountRepository.findOneByAccountNumber(mockRequest.getAccountNumber())).thenReturn(mockSavingAccount);

            DepositResult actualResult = savingAccountService.deposit(mockRequest);

            assertThat(actualResult, instanceOf(DepositResult.Success.class));
            DepositResult.Success successResult = (DepositResult.Success) actualResult;
            assertEquals(mockRequest.getAccountNumber(), successResult.getAccountNumber());
            assertEquals(mockSavingAccount.getBalance(), successResult.getNewBalance());
        }

        @Test
        public void should_not_deposit_if_the_deposit_amount_is_less_than_one() {
            DepositRequest mockRequest = new DepositRequest(BigDecimal.ZERO, "5524867");

            DepositResult actualResult = savingAccountService.deposit(mockRequest);

            assertThat(actualResult, instanceOf(DepositResult.DepositAmountIsLessThanOne.class));
        }

        @Test
        public void should_not_deposit_if_the_saving_account_is_not_found() {
            DepositRequest mockRequest = new DepositRequest(BigDecimal.valueOf(200.00), "2254888");
            when(mockSavingAccountRepository.findOneByAccountNumber(mockRequest.getAccountNumber())).thenReturn(null);

            DepositResult actualResult = savingAccountService.deposit(mockRequest);

            assertThat(actualResult, instanceOf(DepositResult.SavingAccountNotFound.class));
        }
    }

    @Nested
    public class TransferTestSuite {

    }

}