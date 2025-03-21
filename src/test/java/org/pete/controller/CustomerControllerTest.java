package org.pete.controller;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.pete.model.request.RegisterCustomerRequest;
import org.pete.model.response.RegisterCustomerResponse;
import org.pete.model.result.RegisterCustomerResult;
import org.pete.service.CustomerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class CustomerControllerTest {

    private final CustomerService mockCustomerService = Mockito.mock(CustomerService.class);
    private final CustomerController customerController = new CustomerController(mockCustomerService);

    @Nested
    public class RegisterCustomerTestSuite {
        @Test
        public void should_return_created_http_status_if_customer_registration_is_successful() {

            when(mockCustomerService.registerCustomer(any(RegisterCustomerRequest.class))).thenReturn(new RegisterCustomerResult.Success());

            ResponseEntity<RegisterCustomerResponse> actualResult = customerController.registerCustomer(new RegisterCustomerRequest());

            RegisterCustomerResponse resBody = actualResult.getBody();
            assertTrue(resBody.isCreated());
            assertNull(resBody.errorMessage());
            assertEquals(HttpStatus.CREATED, actualResult.getStatusCode());

        }

        @Test
        public void should_return_ok_http_status_if_customer_has_already_existed() {
            when(mockCustomerService.registerCustomer(any(RegisterCustomerRequest.class))).thenReturn(new RegisterCustomerResult.CustAlreadyExists());

            ResponseEntity<RegisterCustomerResponse> actualResult = customerController.registerCustomer(new RegisterCustomerRequest());

            RegisterCustomerResponse resBody = actualResult.getBody();
            assertFalse(resBody.isCreated());
            assertNull(resBody.errorMessage());
            assertEquals(HttpStatus.OK, actualResult.getStatusCode());
        }

        @Test
        public void should_return_bad_request_http_status_if_the_validation_fails() {
            when(mockCustomerService.registerCustomer(any(RegisterCustomerRequest.class))).thenReturn(new RegisterCustomerResult.ValidationFails("Error message"));

            ResponseEntity<RegisterCustomerResponse> actualResult = customerController.registerCustomer(new RegisterCustomerRequest());

            RegisterCustomerResponse resBody = actualResult.getBody();
            assertFalse(resBody.isCreated());
            assertEquals("Error message", resBody.errorMessage());
            assertEquals(HttpStatus.BAD_REQUEST, actualResult.getStatusCode());
        }
    }
}