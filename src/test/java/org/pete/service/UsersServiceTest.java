package org.pete.service;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.pete.constant.Role;
import org.pete.entity.Users;
import org.pete.model.request.UserLoginRequest;
import org.pete.model.request.RegisterCustomerRequest;
import org.pete.model.result.UserLoginResult;
import org.pete.model.result.RegisterCustomerResult;
import org.pete.repository.UserRepository;
import org.pete.validator.UserInfoValidator;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class UsersServiceTest {
    private final UserRepository mockUserRepository = Mockito.mock(UserRepository.class);
    private final UserInfoValidator mockUserInfoValidator = Mockito.mock(UserInfoValidator.class);
    private final BCryptPasswordEncoder mockBCryptPasswordEncoder = Mockito.mock(BCryptPasswordEncoder.class);
    private final UserService userService = new UserService(
            mockUserRepository,
            mockUserInfoValidator,
            mockBCryptPasswordEncoder
    );

    @Nested
    public class RegisterCustomerTestSuite {
        @Test
        public void should_register_customer_successfully() {
            RegisterCustomerRequest mockCustomerRequest = mockRegisterCustomerRequest();
            ArgumentCaptor<Users> customerArgumentCaptor = ArgumentCaptor.forClass(Users.class);
            when(mockUserRepository.findOneByEmailOrCitizenId(anyString(), anyString())).thenReturn(null);
            when(mockUserRepository.save(any())).thenReturn(null);
            when(mockBCryptPasswordEncoder.encode(anyString())).thenAnswer(input -> input.getArguments()[0]);
            when(mockUserInfoValidator.validateEmail(anyString())).thenReturn(true);
            when(mockUserInfoValidator.validateName(anyString())).thenReturn(true);
            when(mockUserInfoValidator.validatePinNumber(anyString())).thenReturn(true);
            when(mockUserInfoValidator.validateCitizenId(anyString())).thenReturn(true);

            RegisterCustomerResult actualResult = userService.registerCustomer(mockCustomerRequest);

            assertThat(actualResult, instanceOf(RegisterCustomerResult.Success.class));
            verify(mockUserRepository, times(1)).save(customerArgumentCaptor.capture());
            Users actualUsers = customerArgumentCaptor.getValue();
            assertCustomerInfo(mockCustomerRequest, actualUsers);
        }

        @Test
        public void should_return_validation_fail_if_citizen_id_validation_does_not_pass() {
            RegisterCustomerRequest mockCustomerRequest = mockRegisterCustomerRequest();
            when(mockUserInfoValidator.validateCitizenId(anyString())).thenReturn(false);

            RegisterCustomerResult actualResult = userService.registerCustomer(mockCustomerRequest);

            assertThat(actualResult, instanceOf(RegisterCustomerResult.ValidationFails.class));
            assertEquals("Citizen Id is not in the correct format.", ((RegisterCustomerResult.ValidationFails)actualResult).getMessage());
            verify(mockUserRepository, times(0)).save(any(Users.class));
        }

        @Test
        public void should_return_validation_fail_if_email_validation_does_not_pass() {
            RegisterCustomerRequest mockCustomerRequest = mockRegisterCustomerRequest();
            when(mockUserInfoValidator.validateCitizenId(anyString())).thenReturn(true);
            when(mockUserInfoValidator.validateEmail(anyString())).thenReturn(false);

            RegisterCustomerResult actualResult = userService.registerCustomer(mockCustomerRequest);

            assertThat(actualResult, instanceOf(RegisterCustomerResult.ValidationFails.class));
            assertEquals("Email is not in the correct format.", ((RegisterCustomerResult.ValidationFails)actualResult).getMessage());
            verify(mockUserRepository, times(0)).save(any(Users.class));
        }

        @Test
        public void should_return_validation_fail_if_thai_name_validation_does_not_pass() {
            RegisterCustomerRequest mockCustomerRequest = mockRegisterCustomerRequest();
            when(mockUserInfoValidator.validateCitizenId(anyString())).thenReturn(true);
            when(mockUserInfoValidator.validateEmail(anyString())).thenReturn(true);
            when(mockUserInfoValidator.validateEmail(mockCustomerRequest.getThaiName())).thenReturn(false);

            RegisterCustomerResult actualResult = userService.registerCustomer(mockCustomerRequest);

            assertThat(actualResult, instanceOf(RegisterCustomerResult.ValidationFails.class));
            assertEquals("Thai name is not in the correct format.", ((RegisterCustomerResult.ValidationFails)actualResult).getMessage());
            verify(mockUserRepository, times(0)).save(any(Users.class));
        }

        @Test
        public void should_return_validation_fail_if_english_name_validation_does_not_pass() {
            RegisterCustomerRequest mockCustomerRequest = mockRegisterCustomerRequest();
            when(mockUserInfoValidator.validateCitizenId(anyString())).thenReturn(true);
            when(mockUserInfoValidator.validateEmail(anyString())).thenReturn(true);
            when(mockUserInfoValidator.validateName(mockCustomerRequest.getThaiName())).thenReturn(true);
            when(mockUserInfoValidator.validateName(mockCustomerRequest.getEnglishName())).thenReturn(false);

            RegisterCustomerResult actualResult = userService.registerCustomer(mockCustomerRequest);

            assertThat(actualResult, instanceOf(RegisterCustomerResult.ValidationFails.class));
            assertEquals("English name is not in the correct format.", ((RegisterCustomerResult.ValidationFails)actualResult).getMessage());
            verify(mockUserRepository, times(0)).save(any(Users.class));
        }

        @Test
        public void should_return_validation_fail_if_pin_number_validation_does_not_pass() {
            RegisterCustomerRequest mockCustomerRequest = mockRegisterCustomerRequest();
            when(mockUserInfoValidator.validateCitizenId(anyString())).thenReturn(true);
            when(mockUserInfoValidator.validateEmail(anyString())).thenReturn(true);
            when(mockUserInfoValidator.validateName(mockCustomerRequest.getThaiName())).thenReturn(true);
            when(mockUserInfoValidator.validateName(mockCustomerRequest.getEnglishName())).thenReturn(true);
            when(mockUserInfoValidator.validatePinNumber(anyString())).thenReturn(false);

            RegisterCustomerResult actualResult = userService.registerCustomer(mockCustomerRequest);

            assertThat(actualResult, instanceOf(RegisterCustomerResult.ValidationFails.class));
            assertEquals("PIN number is not in the correct format.", ((RegisterCustomerResult.ValidationFails)actualResult).getMessage());
            verify(mockUserRepository, times(0)).save(any(Users.class));
        }

        @Test
        public void should_return_customer_exists_if_the_customer_is_found_on_DB() {
            RegisterCustomerRequest mockCustomerRequest = mockRegisterCustomerRequest();
            when(mockUserRepository.findOneByEmailOrCitizenId(anyString(), anyString())).thenReturn(new Users());
            when(mockUserInfoValidator.validateEmail(anyString())).thenReturn(true);
            when(mockUserInfoValidator.validateName(anyString())).thenReturn(true);
            when(mockUserInfoValidator.validatePinNumber(anyString())).thenReturn(true);
            when(mockUserInfoValidator.validateCitizenId(anyString())).thenReturn(true);

            RegisterCustomerResult actualResult = userService.registerCustomer(mockCustomerRequest);

            assertThat(actualResult, instanceOf(RegisterCustomerResult.CustAlreadyExists.class));
            verify(mockUserRepository, times(0)).save(any(Users.class));
        }

        private void assertCustomerInfo(RegisterCustomerRequest mockCustomerRequest, Users actualUsers) {
            assertEquals(mockCustomerRequest.getCitizenId().trim(), actualUsers.getCitizenId());
            assertEquals(mockCustomerRequest.getEmail().trim(), actualUsers.getEmail());
            assertEquals(mockCustomerRequest.getPassword(), actualUsers.getPassword());
            assertEquals(mockCustomerRequest.getThaiName().trim(), actualUsers.getThaiName());
            assertEquals(mockCustomerRequest.getEnglishName().trim(), actualUsers.getEnglishName());
            assertEquals(mockCustomerRequest.getPinNum().trim(), actualUsers.getPinNum());
            assertEquals(List.of(Role.CUSTOMER).toString(), actualUsers.getRole());
        }
    }

    @Nested
    public class CustomerLoginTestSuite {
        @Test
        public void should_login_successfully() {
            UserLoginRequest mockRequest = new UserLoginRequest("testEmail", "testPassword");
            Users mockUsers = mockUser("testEmail", "testPassword");
            when(mockUserRepository.findOneByEmail(mockRequest.getEmail())).thenReturn(mockUsers);
            when(mockBCryptPasswordEncoder.matches(mockUsers.getPassword(), mockRequest.getPassword())).thenReturn(true);

            UserLoginResult actualResult = userService.login(mockRequest);

            assertThat(actualResult, instanceOf(UserLoginResult.LoginSuccess.class));
        }

        @Test
        public void should_login_fails_if_user_is_not_found() {
            UserLoginRequest mockRequest = new UserLoginRequest("testEmail", "testPassword");
            when(mockUserRepository.findOneByEmail(mockRequest.getEmail())).thenReturn(null);

            UserLoginResult actualResult = userService.login(mockRequest);

            assertThat(actualResult, instanceOf(UserLoginResult.UserNotFound.class));
        }

        @Test
        public void should_login_fails_if_password_is_wrong() {
            UserLoginRequest mockRequest = new UserLoginRequest("testEmail", "testPassword");
            Users mockUsers = mockUser("testEmail", "wrongPassword");
            when(mockUserRepository.findOneByEmail(mockRequest.getEmail())).thenReturn(mockUsers);
            when(mockBCryptPasswordEncoder.matches(mockUsers.getPassword(), mockRequest.getPassword())).thenReturn(false);

            UserLoginResult actualResult = userService.login(mockRequest);

            assertThat(actualResult, instanceOf(UserLoginResult.WrongPassword.class));
        }

        private Users mockUser(String email, String password) {
            Users users = new Users();
            users.setEmail(email);
            users.setPassword(password);

            return users;
        }
    }

    private RegisterCustomerRequest mockRegisterCustomerRequest() {
        RegisterCustomerRequest registerCustomerRequest = new RegisterCustomerRequest();

        registerCustomerRequest.setThaiName("ชื่อภาษาไทย");
        registerCustomerRequest.setEnglishName("English name");
        registerCustomerRequest.setEmail("test@email.com");
        registerCustomerRequest.setPassword("testPassword");
        registerCustomerRequest.setCitizenId("1234567890123");
        registerCustomerRequest.setPinNum("123456");
        return registerCustomerRequest;
    }
}