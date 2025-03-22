package org.pete.service;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.pete.entity.Customer;
import org.pete.entity.SavingAccount;
import org.pete.model.request.CreateSavingAccountRequest;
import org.pete.model.result.CreateSavingAccountResult;
import org.pete.repository.CustomerRepository;
import org.pete.repository.SavingAccountRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.math.BigDecimal;

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
        public void should_create_account_successfully() {
            System.out.println("should_create_account_successfully() name  =>  " + Thread.currentThread().getName());
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
            verify(mockSavingAccountRepository, times(1)).save(savingAccountArgumentCaptor.capture());
            assertSavingAccountInfo(mockRequest, savingAccountArgumentCaptor.getValue(), mockAccountNumber, mockCustomer);
        }

        @Test
        public void should_return_customer_not_found_if_customer_does_not_exist() {
            System.out.println("should_return_customer_not_found_if_customer_does_not_exist() name  =>  " + Thread.currentThread().getName());
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
            assertEquals(mockRequest.getDepositAmount(), savingAccount.getBalance());
            assertEquals(mockAccountNumber.toString(), savingAccount.getAccountNumber());
            assertEquals(mockCustomer, savingAccount.getCustomer());
        }
    }

    @Nested
    public class DepositTestSuite {

    }

    @Nested
    public class TransferTestSuite {

    }

}