package org.pete.controller;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.pete.entity.Users;
import org.pete.model.principle.UserPrinciple;
import org.pete.model.request.CreateSavingAccountRequest;
import org.pete.model.request.DepositRequest;
import org.pete.model.request.TransferRequest;
import org.pete.model.response.CreateSavingAccountResponse;
import org.pete.model.response.DepositResponse;
import org.pete.model.response.FindSavingAccountInfoResponse;
import org.pete.model.response.TransferResponse;
import org.pete.model.result.CreateSavingAccountResult;
import org.pete.model.result.DepositResult;
import org.pete.model.result.FindSavingAccountInfoResult;
import org.pete.model.result.TransferResult;
import org.pete.service.SavingAccountService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

public class SavingAccountsControllerTest {

    private final SavingAccountService mockSavingAccountService = Mockito.mock(SavingAccountService.class);
    private final SavingAccountController savingAccountController = new SavingAccountController(mockSavingAccountService);

    @Nested
    public class CreateSavingAccountsTestSuite {
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
        public void should_return_bad_request_http_status_if_the_amount_is_negative() {
            CreateSavingAccountRequest mockRequest = new CreateSavingAccountRequest();
            mockRequest.setEnglishName("Negative amount");
            CreateSavingAccountResult.AmountIsNegative mockResult =  new CreateSavingAccountResult.AmountIsNegative();
            when(mockSavingAccountService.createSavingAccount(mockRequest)).thenReturn(mockResult);

            ResponseEntity<CreateSavingAccountResponse> response = savingAccountController.createSavingAccount(mockRequest);

            CreateSavingAccountResponse actualResBody = response.getBody();
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertNull(actualResBody.accountNumber());
            assertNull(actualResBody.currentBalance());
            assertEquals("Amount must be positive.", actualResBody.errorMessage());
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
        @Test
        public void should_return_ok_http_status_when_depositing_successfully() {
            DepositRequest mockRequest = new DepositRequest();
            mockRequest.setAccountNumber("1000001");
            mockRequest.setDepositAmount(BigDecimal.ONE);
            DepositResult.Success mockResult =  new DepositResult.Success("1000001", BigDecimal.TEN);
            when(mockSavingAccountService.deposit(mockRequest)).thenReturn(mockResult);

            ResponseEntity<DepositResponse> response = savingAccountController.deposit(mockRequest);

            DepositResponse actualResBody = response.getBody();
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(mockResult.getAccountNumber(), actualResBody.accountNumber());
            assertEquals(mockResult.getNewBalance(), actualResBody.newBalance());
            assertNull(actualResBody.errorMessage());
        }

        @Test
        public void should_return_not_found_http_status_when_saving_account_is_not_found() {
            DepositRequest mockRequest = new DepositRequest();
            mockRequest.setAccountNumber("1000002");
            mockRequest.setDepositAmount(BigDecimal.ONE);
            DepositResult.SavingAccountNotFound mockResult =  new DepositResult.SavingAccountNotFound();
            when(mockSavingAccountService.deposit(mockRequest)).thenReturn(mockResult);

            ResponseEntity<DepositResponse> response = savingAccountController.deposit(mockRequest);

            DepositResponse actualResBody = response.getBody();
            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
            assertNull(actualResBody.accountNumber());
            assertNull(actualResBody.newBalance());
            assertEquals("Invalid account number.", actualResBody.errorMessage());
        }

        @Test
        public void should_return_bad_request_http_status_if_deposit_amount_is_less_than_one() {
            DepositRequest mockRequest = new DepositRequest();
            mockRequest.setAccountNumber("1000003");
            mockRequest.setDepositAmount(BigDecimal.ZERO);
            DepositResult.DepositAmountIsLessThanOne mockResult =  new DepositResult.DepositAmountIsLessThanOne();
            when(mockSavingAccountService.deposit(mockRequest)).thenReturn(mockResult);

            ResponseEntity<DepositResponse> response = savingAccountController.deposit(mockRequest);

            DepositResponse actualResBody = response.getBody();
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertNull(actualResBody.accountNumber());
            assertNull(actualResBody.newBalance());
            assertEquals("Deposit amount must be more than one.", actualResBody.errorMessage());
        }

        @Test
        public void should_return_unprocessable_entity_http_status_by_default() {
            DepositRequest mockRequest = new DepositRequest();
            mockRequest.setAccountNumber("1000004");
            mockRequest.setDepositAmount(BigDecimal.valueOf(500L));
            when(mockSavingAccountService.deposit(mockRequest)).thenReturn(new DepositResult());

            ResponseEntity<DepositResponse> response = savingAccountController.deposit(mockRequest);

            DepositResponse actualResBody = response.getBody();
            assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
            assertNull(actualResBody);
        }

    }

    @Nested
    public class TransferTestSuite {
        @Test
        public void should_return_ok_status_when_transferring_money_is_success() {
            Users mockUser = mockUser(3L);
            UserPrinciple mockUserPrinciple = new UserPrinciple(mockUser);
            Authentication mockAuthentication = Mockito.mock(Authentication.class);
            String mockSenderAccountNumber = "mockSenderAccountNumber";
            String mockBeneficiaryAccountNumber = "mockBeneficiaryAccount";
            BigDecimal mockSenderBalance = BigDecimal.TWO;
            BigDecimal mockBeneficiaryBalance = BigDecimal.TEN;
            TransferRequest mockRequest = new TransferRequest();
            when(mockAuthentication.getPrincipal()).thenReturn(mockUserPrinciple);
            when(mockSavingAccountService.transfer(mockRequest, mockUser.getId()))
                    .thenReturn(new TransferResult.Success(
                            mockSenderAccountNumber,
                            mockBeneficiaryAccountNumber,
                            mockSenderBalance,
                            mockBeneficiaryBalance
                    ));

            ResponseEntity<TransferResponse> actualResponse = savingAccountController.transfer(mockRequest, mockAuthentication);

            assertEquals(HttpStatus.OK, actualResponse.getStatusCode());
            TransferResponse result = actualResponse.getBody();
            assertEquals(mockSenderAccountNumber, result.senderAccountNumber());
            assertEquals(mockBeneficiaryAccountNumber, result.beneficiaryAccountNumber());
            assertEquals(mockSenderBalance, result.currentSenderBalance());
            assertEquals(mockBeneficiaryBalance, result.currentBeneficiaryBalance());
            assertNull(result.errorMessage());
        }

        @Test
        public void should_return_not_found_status_if_saving_account_is_not_found() {
            String mockErrorMessage = "Account is not found";
            Users mockUser = mockUser(4L);
            UserPrinciple mockUserPrinciple = new UserPrinciple(mockUser);
            Authentication mockAuthentication = Mockito.mock(Authentication.class);
            TransferRequest mockRequest = new TransferRequest();
            when(mockAuthentication.getPrincipal()).thenReturn(mockUserPrinciple);
            when(mockSavingAccountService.transfer(mockRequest, mockUser.getId()))
                    .thenReturn(new TransferResult.SavingAccountNotFound(mockErrorMessage));

            ResponseEntity<TransferResponse> actualResponse = savingAccountController.transfer(mockRequest, mockAuthentication);

            assertEquals(HttpStatus.NOT_FOUND, actualResponse.getStatusCode());
            TransferResponse result = actualResponse.getBody();
            assertNull(result.senderAccountNumber());
            assertNull(result.beneficiaryAccountNumber());
            assertNull(result.currentSenderBalance());
            assertNull(result.currentBeneficiaryBalance());
            assertEquals(mockErrorMessage, result.errorMessage());
        }

        @Test
        public void should_return_bad_request_status_if_saving_account_does_not_have_enough_balance() {
            String mockNotEnoughBalanceAccount = "notEnoughBalanceAccount";
            BigDecimal mockBalance = BigDecimal.TWO;
            Users mockUser = mockUser(7L);
            UserPrinciple mockUserPrinciple = new UserPrinciple(mockUser);
            Authentication mockAuthentication = Mockito.mock(Authentication.class);
            TransferRequest mockRequest = new TransferRequest();
            when(mockAuthentication.getPrincipal()).thenReturn(mockUserPrinciple);
            when(mockSavingAccountService.transfer(mockRequest, mockUser.getId()))
                    .thenReturn(new TransferResult.NotEnoughBalance(mockNotEnoughBalanceAccount,mockBalance));

            ResponseEntity<TransferResponse> actualResponse = savingAccountController.transfer(mockRequest, mockAuthentication);

            assertEquals(HttpStatus.BAD_REQUEST, actualResponse.getStatusCode());
            TransferResponse result = actualResponse.getBody();
            assertEquals(mockNotEnoughBalanceAccount, result.senderAccountNumber());
            assertNull(result.beneficiaryAccountNumber());
            assertEquals(mockBalance, result.currentSenderBalance());
            assertNull(result.currentBeneficiaryBalance());
            assertNull(result.errorMessage());
        }

        @Test
        public void should_return_unprocess_entity_status_if_user_principle_is_not_found() {
            Authentication mockAuthentication = Mockito.mock(Authentication.class);
            TransferRequest mockRequest = new TransferRequest();
            when(mockAuthentication.getPrincipal()).thenReturn(null);

            ResponseEntity<TransferResponse> actualResponse = savingAccountController.transfer(mockRequest, mockAuthentication);

            assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, actualResponse.getStatusCode());
            assertNull(actualResponse.getBody());
        }

        @ParameterizedTest
        @MethodSource("notPassValidationResults")
        public void should_return_bad_request_status_if_validation_does_not_pass(TransferResult transferResult) {
            Users mockUser = mockUser(10L);
            UserPrinciple mockUserPrinciple = new UserPrinciple(mockUser);
            Authentication mockAuthentication = Mockito.mock(Authentication.class);
            TransferRequest mockRequest = new TransferRequest();
            when(mockAuthentication.getPrincipal()).thenReturn(mockUserPrinciple);
            when(mockSavingAccountService.transfer(mockRequest, mockUser.getId()))
                    .thenReturn(transferResult);

            ResponseEntity<TransferResponse> actualResponse = savingAccountController.transfer(mockRequest, mockAuthentication);

            assertEquals(HttpStatus.BAD_REQUEST, actualResponse.getStatusCode());
            TransferResponse result = actualResponse.getBody();
            assertNull(result.senderAccountNumber());
            assertNull(result.beneficiaryAccountNumber());
            assertNull(result.currentSenderBalance());
            assertNull(result.currentBeneficiaryBalance());
            assertEquals(
                    TransferResult.getErrorMessageFromResultType(transferResult),
                    result.errorMessage()
            );
        }

        static List<TransferResult> notPassValidationResults() {
            return List.of(
                    new TransferResult.TransferAmountIsLessThanOne(),
                    new TransferResult.WrongPinNumber(),
                    new TransferResult.WrongSenderAccount(),
                    new TransferResult.NotPinNumberProvided()
            );
        }
    }

    @Nested
    public class FindingAccountInfoTestSuite {
        @Test
        public void should_return_ok_status_if_getting_account_info_successfully() {
            Long mockUserId = 5L;
            String mockAccountNumber = "accountNumber";
            Users mockUser = mockUser(mockUserId);
            UserPrinciple mockUserPrinciple = new UserPrinciple(mockUser);
            Authentication mockAuthentication = Mockito.mock(Authentication.class);
            when(mockAuthentication.getPrincipal()).thenReturn(mockUserPrinciple);
            when(mockSavingAccountService.findSavingAccountInfo(mockAccountNumber, mockUserId))
                    .thenReturn(new FindSavingAccountInfoResult.Success(
                            mockAccountNumber,
                            BigDecimal.TEN,
                            LocalDateTime.now(),
                            LocalDateTime.now()
                    ));

            ResponseEntity<FindSavingAccountInfoResponse> actualResponse =
                    savingAccountController.findSavingAccountInfo(mockAccountNumber, mockAuthentication);

            assertEquals(HttpStatus.OK, actualResponse.getStatusCode());
            FindSavingAccountInfoResponse responseBody = actualResponse.getBody();
            assertEquals(mockAccountNumber, responseBody.accountNumber());
            assertEquals(BigDecimal.TEN, responseBody.currentBalance());
            assertNotNull(responseBody.creationDate());
            assertNotNull(responseBody.latestUpdateDate());
        }

        @Test
        public void should_return_not_found_status_if_account_is_not_found() {
            Long mockUserId = 6L;
            String mockAccountNumber = "invalidAccountNumber";
            Users mockUser = mockUser(mockUserId);
            UserPrinciple mockUserPrinciple = new UserPrinciple(mockUser);
            Authentication mockAuthentication = Mockito.mock(Authentication.class);
            when(mockAuthentication.getPrincipal()).thenReturn(mockUserPrinciple);
            when(mockSavingAccountService.findSavingAccountInfo(mockAccountNumber, mockUserId))
                    .thenReturn(new FindSavingAccountInfoResult.AccountNotFound());

            ResponseEntity<FindSavingAccountInfoResponse> actualResponse =
                    savingAccountController.findSavingAccountInfo(mockAccountNumber, mockAuthentication);

            assertEquals(HttpStatus.NOT_FOUND, actualResponse.getStatusCode());
            assertNull(actualResponse.getBody());
        }

        @Test
        public void should_return_not_found_status_if_requester_id_is_not_the_same_in_the_account() {
            Long mockUserId = 7L;
            String mockAccountNumber = "wrongAccountNumber";
            Users mockUser = mockUser(mockUserId);
            UserPrinciple mockUserPrinciple = new UserPrinciple(mockUser);
            Authentication mockAuthentication = Mockito.mock(Authentication.class);
            when(mockAuthentication.getPrincipal()).thenReturn(mockUserPrinciple);
            when(mockSavingAccountService.findSavingAccountInfo(mockAccountNumber, mockUserId))
                    .thenReturn(new FindSavingAccountInfoResult.AccountNotFound());

            ResponseEntity<FindSavingAccountInfoResponse> actualResponse =
                    savingAccountController.findSavingAccountInfo(mockAccountNumber, mockAuthentication);

            assertEquals(HttpStatus.NOT_FOUND, actualResponse.getStatusCode());
            assertNull(actualResponse.getBody());
        }

        @Test
        public void should_return_unprocessable_entity_status_if_user_principle_is_not_found() {
            Authentication mockAuthentication = Mockito.mock(Authentication.class);
            when(mockAuthentication.getPrincipal()).thenReturn(null);

            ResponseEntity<FindSavingAccountInfoResponse> actualResponse =
                    savingAccountController.findSavingAccountInfo("anyAccountNumber", mockAuthentication);

            assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, actualResponse.getStatusCode());
            assertNull(actualResponse.getBody());
        }
    }

    private Users mockUser(Long userId) {
        Users users = new Users();
        users.setId(userId);

        return users;
    }
}