package org.pete.controller;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.pete.model.request.CreateSavingAccountRequest;
import org.pete.model.response.CreateSavingAccountResponse;
import org.pete.model.result.CreateSavingAccountResult;
import org.pete.service.SavingAccountService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

public class SavingAccountControllerTest {

    private final SavingAccountService mockSavingAccountService = Mockito.mock(SavingAccountService.class);
    private final SavingAccountController savingAccountController = new SavingAccountController(mockSavingAccountService);

    @Nested
    public class CreateSavingAccountTestSuite {
        @Test
        public void should_return_created_http_status_when_saving_account_is_created_successfully() {
            CreateSavingAccountRequest mockRequest = new CreateSavingAccountRequest();
            mockRequest.setEnglishName("Success saving account");
            CreateSavingAccountResult.Success mockResult =  new CreateSavingAccountResult.Success("1000001", BigDecimal.TEN);
            when(mockSavingAccountService.createSavingAccount(mockRequest)).thenReturn(mockResult);

            ResponseEntity<CreateSavingAccountResponse> response = savingAccountController.createSavingAccount(mockRequest);

            CreateSavingAccountResponse actualResBody = response.getBody();
            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertEquals(mockResult.getAccountNumber(), actualResBody.accountNumber());
            assertEquals(mockResult.getCurrentBalance(), actualResBody.currentBalance());
            assertNull(actualResBody.errorMessage());
        }

        @Test
        public void should_return_not_found_http_status_when_customer_does_not_exist() {
            CreateSavingAccountRequest mockRequest = new CreateSavingAccountRequest();
            mockRequest.setEnglishName("Non-existing customer");
            CreateSavingAccountResult.CustNotFound mockResult =  new CreateSavingAccountResult.CustNotFound();
            when(mockSavingAccountService.createSavingAccount(mockRequest)).thenReturn(mockResult);

            ResponseEntity<CreateSavingAccountResponse> response = savingAccountController.createSavingAccount(mockRequest);

            CreateSavingAccountResponse actualResBody = response.getBody();
            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
            assertNull(actualResBody.accountNumber());
            assertNull(actualResBody.currentBalance());
            assertEquals("Customer is not found.", actualResBody.errorMessage());
        }

        @Test
        public void should_return_unprocessable_entity_http_status_by_default() {
            CreateSavingAccountRequest mockRequest = new CreateSavingAccountRequest();
            mockRequest.setEnglishName("Some random happen");
            when(mockSavingAccountService.createSavingAccount(mockRequest)).thenReturn(new CreateSavingAccountResult());

            ResponseEntity<CreateSavingAccountResponse> response = savingAccountController.createSavingAccount(mockRequest);

            CreateSavingAccountResponse actualResBody = response.getBody();
            assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
            assertNull(actualResBody);
        }
    }

    @Nested
    public class DepositTestSuite {

    }

    @Nested
    public class TransferTestSuite {

    }

    @Nested
    public class FindingAccountTestSuite {

    }
}