package org.pete.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.pete.entity.Customer;
import org.pete.model.request.RegisterCustomerRequest;
import org.pete.model.result.RegisterCustomerResult;
import org.pete.repository.CustomerRepository;
import org.pete.validator.CustomerInfoValidator;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;


public class CustomerServiceTest {
    private final CustomerRepository mockCustomerRepository = Mockito.mock(CustomerRepository.class);
    private final CustomerInfoValidator mockCustomerInfoValidator = Mockito.mock(CustomerInfoValidator.class);
    private final BCryptPasswordEncoder mockBCryptPasswordEncoder = Mockito.mock(BCryptPasswordEncoder.class);
    private final CustomerService customerService = new CustomerService(
            mockCustomerRepository,
            mockCustomerInfoValidator,
            mockBCryptPasswordEncoder
    );

    @Nested
    public class RegisterCustomerTestSuite{
        @Test
        public void should_register_customer_successfully() {
            RegisterCustomerRequest mockCustomerRequest = mockRegisterCustomerRequest();
            ArgumentCaptor<Customer> customerArgumentCaptor = ArgumentCaptor.forClass(Customer.class);
            when(mockCustomerRepository.findOneByEmailOrCitizenId(anyString(), anyString())).thenReturn(null);
            when(mockCustomerRepository.save(any())).thenReturn(null);
            when(mockBCryptPasswordEncoder.encode(anyString())).thenAnswer(input -> input.getArguments()[0]);
            when(mockCustomerInfoValidator.validateEmail(anyString())).thenReturn(true);
            when(mockCustomerInfoValidator.validateName(anyString())).thenReturn(true);
            when(mockCustomerInfoValidator.validatePinNumber(anyString())).thenReturn(true);
            when(mockCustomerInfoValidator.validateCitizenId(anyString())).thenReturn(true);

            RegisterCustomerResult actualResult = customerService.registerCustomer(mockCustomerRequest);

            assertThat(actualResult, instanceOf(RegisterCustomerResult.Success.class));
            verify(mockCustomerRepository, times(1)).save(customerArgumentCaptor.capture());
            Customer actualCustomer = customerArgumentCaptor.getValue();
            assertCustomerInfo(mockCustomerRequest, actualCustomer);
        }

        @Test
        public void should_return_validation_fail_if_citizen_id_validation_does_not_pass() {
            RegisterCustomerRequest mockCustomerRequest = mockRegisterCustomerRequest();
            when(mockCustomerInfoValidator.validateCitizenId(anyString())).thenReturn(false);

            RegisterCustomerResult actualResult = customerService.registerCustomer(mockCustomerRequest);

            assertThat(actualResult, instanceOf(RegisterCustomerResult.ValidationFails.class));
            assertEquals("Citizen Id is not in the correct format.", ((RegisterCustomerResult.ValidationFails)actualResult).getMessage());
            verify(mockCustomerRepository, times(0)).save(any(Customer.class));
        }

        @Test
        public void should_return_validation_fail_if_email_validation_does_not_pass() {
            RegisterCustomerRequest mockCustomerRequest = mockRegisterCustomerRequest();
            when(mockCustomerInfoValidator.validateCitizenId(anyString())).thenReturn(true);
            when(mockCustomerInfoValidator.validateEmail(anyString())).thenReturn(false);

            RegisterCustomerResult actualResult = customerService.registerCustomer(mockCustomerRequest);

            assertThat(actualResult, instanceOf(RegisterCustomerResult.ValidationFails.class));
            assertEquals("Email is not in the correct format.", ((RegisterCustomerResult.ValidationFails)actualResult).getMessage());
            verify(mockCustomerRepository, times(0)).save(any(Customer.class));
        }

        @Test
        public void should_return_validation_fail_if_thai_name_validation_does_not_pass() {
            RegisterCustomerRequest mockCustomerRequest = mockRegisterCustomerRequest();
            when(mockCustomerInfoValidator.validateCitizenId(anyString())).thenReturn(true);
            when(mockCustomerInfoValidator.validateEmail(anyString())).thenReturn(true);
            when(mockCustomerInfoValidator.validateEmail(mockCustomerRequest.getThaiName())).thenReturn(false);

            RegisterCustomerResult actualResult = customerService.registerCustomer(mockCustomerRequest);

            assertThat(actualResult, instanceOf(RegisterCustomerResult.ValidationFails.class));
            assertEquals("Thai name is not in the correct format.", ((RegisterCustomerResult.ValidationFails)actualResult).getMessage());
            verify(mockCustomerRepository, times(0)).save(any(Customer.class));
        }

        @Test
        public void should_return_validation_fail_if_english_name_validation_does_not_pass() {
            RegisterCustomerRequest mockCustomerRequest = mockRegisterCustomerRequest();
            when(mockCustomerInfoValidator.validateCitizenId(anyString())).thenReturn(true);
            when(mockCustomerInfoValidator.validateEmail(anyString())).thenReturn(true);
            when(mockCustomerInfoValidator.validateName(mockCustomerRequest.getThaiName())).thenReturn(true);
            when(mockCustomerInfoValidator.validateName(mockCustomerRequest.getEnglishName())).thenReturn(false);

            RegisterCustomerResult actualResult = customerService.registerCustomer(mockCustomerRequest);

            assertThat(actualResult, instanceOf(RegisterCustomerResult.ValidationFails.class));
            assertEquals("English name is not in the correct format.", ((RegisterCustomerResult.ValidationFails)actualResult).getMessage());
            verify(mockCustomerRepository, times(0)).save(any(Customer.class));
        }

        @Test
        public void should_return_validation_fail_if_pin_number_validation_does_not_pass() {
            RegisterCustomerRequest mockCustomerRequest = mockRegisterCustomerRequest();
            when(mockCustomerInfoValidator.validateCitizenId(anyString())).thenReturn(true);
            when(mockCustomerInfoValidator.validateEmail(anyString())).thenReturn(true);
            when(mockCustomerInfoValidator.validateName(mockCustomerRequest.getThaiName())).thenReturn(true);
            when(mockCustomerInfoValidator.validateName(mockCustomerRequest.getEnglishName())).thenReturn(true);
            when(mockCustomerInfoValidator.validatePinNumber(anyString())).thenReturn(false);

            RegisterCustomerResult actualResult = customerService.registerCustomer(mockCustomerRequest);

            assertThat(actualResult, instanceOf(RegisterCustomerResult.ValidationFails.class));
            assertEquals("PIN number is not in the correct format.", ((RegisterCustomerResult.ValidationFails)actualResult).getMessage());
            verify(mockCustomerRepository, times(0)).save(any(Customer.class));
        }

        @Test
        public void should_return_customer_exists_if_the_customer_is_found_on_DB() {
            RegisterCustomerRequest mockCustomerRequest = mockRegisterCustomerRequest();
            when(mockCustomerRepository.findOneByEmailOrCitizenId(anyString(), anyString())).thenReturn(new Customer());
            when(mockCustomerInfoValidator.validateEmail(anyString())).thenReturn(true);
            when(mockCustomerInfoValidator.validateName(anyString())).thenReturn(true);
            when(mockCustomerInfoValidator.validatePinNumber(anyString())).thenReturn(true);
            when(mockCustomerInfoValidator.validateCitizenId(anyString())).thenReturn(true);

            RegisterCustomerResult actualResult = customerService.registerCustomer(mockCustomerRequest);

            assertThat(actualResult, instanceOf(RegisterCustomerResult.CustAlreadyExists.class));
            verify(mockCustomerRepository, times(0)).save(any(Customer.class));
        }

        private void assertCustomerInfo(RegisterCustomerRequest mockCustomerRequest, Customer actualCustomer) {
            assertEquals(mockCustomerRequest.getCitizenId().trim(), actualCustomer.getCitizenId());
            assertEquals(mockCustomerRequest.getEmail().trim(), actualCustomer.getEmail());
            assertEquals(mockCustomerRequest.getPassword(), actualCustomer.getPassword());
            assertEquals(mockCustomerRequest.getThaiName().trim(), actualCustomer.getThaiName());
            assertEquals(mockCustomerRequest.getEnglishName().trim(), actualCustomer.getEnglishName());
            assertEquals(mockCustomerRequest.getPinNum().trim(), actualCustomer.getPinNum());
            assertNotNull(actualCustomer.getCreationDate());
            assertNotNull(actualCustomer.getLastUpdateDate());
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

    @AfterEach
    public void tearDown() {
        Mockito.reset(mockCustomerRepository);
        Mockito.reset(mockCustomerInfoValidator);
        Mockito.reset(mockBCryptPasswordEncoder);
    }
}