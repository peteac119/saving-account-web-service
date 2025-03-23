package org.pete.controller;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.pete.model.request.UserLoginRequest;
import org.pete.model.result.UserLoginResult;
import org.pete.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class TellerControllerTest {

    private final UserService mockUserService = Mockito.mock(UserService.class);
    private final TellerController tellerController = new TellerController(mockUserService);

    @Nested
    public class LoginTestSuite {
        @Test
        public void should_return_ok_status_after_login_successfully() {
            UserLoginRequest mockRequest = new UserLoginRequest("testEmail", "testPassword");
            when(mockUserService.login(mockRequest)).thenReturn(new UserLoginResult.LoginSuccess());

            ResponseEntity<?> actualResponse = tellerController.tellerLogin(mockRequest);

            assertEquals(HttpStatus.OK, actualResponse.getStatusCode());
        }

        @Test
        public void should_return_forbidden_status_if_password_is_wrong() {
            UserLoginRequest mockRequest = new UserLoginRequest("testEmail", "testPassword");
            when(mockUserService.login(mockRequest)).thenReturn(new UserLoginResult.WrongPassword());

            ResponseEntity<?> actualResponse = tellerController.tellerLogin(mockRequest);

            assertEquals(HttpStatus.FORBIDDEN, actualResponse.getStatusCode());
        }

        @Test
        public void should_return_not_found_status_if_the_user_is_not_found() {
            UserLoginRequest mockRequest = new UserLoginRequest("testEmail", "testPassword");
            when(mockUserService.login(mockRequest)).thenReturn(new UserLoginResult.UserNotFound());

            ResponseEntity<?> actualResponse = tellerController.tellerLogin(mockRequest);

            assertEquals(HttpStatus.NOT_FOUND, actualResponse.getStatusCode());
        }
    }
}