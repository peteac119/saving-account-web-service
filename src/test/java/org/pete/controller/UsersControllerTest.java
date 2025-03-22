package org.pete.controller;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.pete.model.request.CustomerLoginRequest;
import org.pete.model.request.RegisterCustomerRequest;
import org.pete.model.response.RegisterCustomerResponse;
import org.pete.model.result.CustomerLoginResult;
import org.pete.model.result.RegisterCustomerResult;
import org.pete.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

public class UsersControllerTest {

    private final UserService mockUserService = Mockito.mock(UserService.class);
    private final UserController userController = new UserController(mockUserService);

    @Nested
    public class RegisterUsersTestSuite {
        @Test
        public void should_return_created_http_status_if_customer_registration_is_successful() {
            RegisterCustomerRequest mockRequest = new RegisterCustomerRequest();
            mockRequest.setEnglishName("Success");
            when(mockUserService.registerCustomer(mockRequest)).thenReturn(new RegisterCustomerResult.Success());

            ResponseEntity<RegisterCustomerResponse> actualResult = userController.registerCustomer(mockRequest);

            RegisterCustomerResponse resBody = actualResult.getBody();
            assertTrue(resBody.isCreated());
            assertNull(resBody.errorMessage());
            assertEquals(HttpStatus.CREATED, actualResult.getStatusCode());

        }

        @Test
        public void should_return_ok_http_status_if_customer_has_already_existed() {
            RegisterCustomerRequest mockRequest = new RegisterCustomerRequest();
            mockRequest.setEnglishName("Already exist");
            when(mockUserService.registerCustomer(mockRequest)).thenReturn(new RegisterCustomerResult.CustAlreadyExists());

            ResponseEntity<RegisterCustomerResponse> actualResult = userController.registerCustomer(mockRequest);

            RegisterCustomerResponse resBody = actualResult.getBody();
            assertFalse(resBody.isCreated());
            assertNull(resBody.errorMessage());
            assertEquals(HttpStatus.OK, actualResult.getStatusCode());
        }

        @Test
        public void should_return_bad_request_http_status_if_the_validation_fails() {
            RegisterCustomerRequest mockRequest = new RegisterCustomerRequest();
            mockRequest.setEnglishName("Validation fails");
            when(mockUserService.registerCustomer(mockRequest)).thenReturn(new RegisterCustomerResult.ValidationFails("Error message"));

            ResponseEntity<RegisterCustomerResponse> actualResult = userController.registerCustomer(mockRequest);

            RegisterCustomerResponse resBody = actualResult.getBody();
            assertFalse(resBody.isCreated());
            assertEquals("Error message", resBody.errorMessage());
            assertEquals(HttpStatus.BAD_REQUEST, actualResult.getStatusCode());
        }
    }

    @Nested
    public class CustomerLoginTestSuite {
        @Test
        public void should_return_ok_status_after_login_successfully() {
            CustomerLoginRequest mockRequest = new CustomerLoginRequest("testEmail", "testPassword");
            when(mockUserService.customerLogin(mockRequest)).thenReturn(new CustomerLoginResult.LoginSuccess());

            ResponseEntity<?> actualResponse = userController.customerLogin(mockRequest);

            assertEquals(HttpStatus.OK, actualResponse.getStatusCode());
        }

        @Test
        public void should_return_forbidden_status_if_password_is_wrong() {
            CustomerLoginRequest mockRequest = new CustomerLoginRequest("testEmail", "testPassword");
            when(mockUserService.customerLogin(mockRequest)).thenReturn(new CustomerLoginResult.WrongPassword());

            ResponseEntity<?> actualResponse = userController.customerLogin(mockRequest);

            assertEquals(HttpStatus.FORBIDDEN, actualResponse.getStatusCode());
        }

        @Test
        public void should_return_not_found_status_if_the_user_is_not_found() {
            CustomerLoginRequest mockRequest = new CustomerLoginRequest("testEmail", "testPassword");
            when(mockUserService.customerLogin(mockRequest)).thenReturn(new CustomerLoginResult.UserNotFound());

            ResponseEntity<?> actualResponse = userController.customerLogin(mockRequest);

            assertEquals(HttpStatus.NOT_FOUND, actualResponse.getStatusCode());
        }
    }
}