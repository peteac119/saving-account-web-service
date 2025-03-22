package org.pete.validator;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UserInfoValidatorTest {

    private final UserInfoValidator userInfoValidator = new UserInfoValidator();

    @Nested
    public class PinNumberValidationTestSuite {
        @Test
        public void normal_pin_number_should_pass_validation() {
            String mockPinNumber = "123456";

            boolean actualResult = userInfoValidator.validatePinNumber(mockPinNumber);

            assertTrue(actualResult);
        }

        @Test
        public void pin_number_with_character_should_not_pass_validation() {
            String mockPinNumber = "12Abd3";

            boolean actualResult = userInfoValidator.validatePinNumber(mockPinNumber);

            assertFalse(actualResult);
        }

        @Test
        public void pin_number_with_the_length_less_than_6_should_not_pass_validation() {
            String mockPinNumber = "1236";

            boolean actualResult = userInfoValidator.validatePinNumber(mockPinNumber);

            assertFalse(actualResult);
        }

        @Test
        public void pin_number_with_the_length_more_than_6_should_not_pass_validation() {
            String mockPinNumber = "112233445566";

            boolean actualResult = userInfoValidator.validatePinNumber(mockPinNumber);

            assertFalse(actualResult);
        }

        @Test
        public void null_value_should_not_pass_validation() {
            boolean actualResult = userInfoValidator.validatePinNumber(null);

            assertFalse(actualResult);
        }
    }

    @Nested
    public class CitizenIdValidationTestSuite {
        @Test
        public void normal_citizen_id_should_pass_validation() {
            String mockCitizenId = "1234567890123";

            boolean actualResult = userInfoValidator.validateCitizenId(mockCitizenId);

            assertTrue(actualResult);
        }

        @Test
        public void citizen_id_with_character_should_not_pass_validation() {
            String mockCitizenId = "12ABCD0123";

            boolean actualResult = userInfoValidator.validateCitizenId(mockCitizenId);

            assertFalse(actualResult);
        }

        @Test
        public void citizen_id_with_the_length_less_than_13_should_not_pass_validation() {
            String mockCitizenId = "123456";

            boolean actualResult = userInfoValidator.validateCitizenId(mockCitizenId);

            assertFalse(actualResult);
        }

        @Test
        public void citizen_id_with_the_length_more_than_13_should_not_pass_validation() {
            String mockCitizenId = "12ABCD0123456";

            boolean actualResult = userInfoValidator.validateCitizenId(mockCitizenId);

            assertFalse(actualResult);
        }

        @Test
        public void null_value_should_not_pass_validation() {
            boolean actualResult = userInfoValidator.validateCitizenId(null);

            assertFalse(actualResult);
        }
    }

    @Nested
    public class NameValidationTestSuite {
        @Test
        public void name_with_the_length_less_than_100_should_pass_validation() {
            String mockName = "TestFirstName TestLastName";
            boolean actualResult = userInfoValidator.validateName(mockName);

            assertTrue(actualResult);
        }

        @Test
        public void name_with_the_length_more_than_100_should_not_pass_validation() {
            String mockName = "TestFirstName TestLastName TestFirstName TestLastName TestFirstName TestLastName TestFirstName TestLastName TestFirstName";
            boolean actualResult = userInfoValidator.validateName(mockName);

            assertFalse(actualResult);
        }

        @Test
        public void null_value_should_not_pass_validation() {
            boolean actualResult = userInfoValidator.validateName(null);

            assertFalse(actualResult);
        }
    }

    @Nested
    public class EmailValidationTestSuite {
        @Test
        public void normal_email_should_pass_validation() {
            String mockEmail = "test_email@address.com";

            boolean actualResult = userInfoValidator.validateEmail(mockEmail);

            assertTrue(actualResult);
        }

        @Test
        public void normal_email_with_multiple_dots_should_pass_validation() {
            String mockEmail = "test_email@firstdot.seconddot.com";

            boolean actualResult = userInfoValidator.validateEmail(mockEmail);

            assertTrue(actualResult);
        }

        @Test
        public void email_without_at_sign_should_not_pass_validation() {
            String mockEmail = "test_email.email.com";

            boolean actualResult = userInfoValidator.validateEmail(mockEmail);

            assertFalse(actualResult);
        }

        @Test
        public void email_with_one_char_of_top_level_domain_should_not_pass_validation() {
            String mockEmail = "test_email@email.c";

            boolean actualResult = userInfoValidator.validateEmail(mockEmail);

            assertFalse(actualResult);
        }

        @Test
        public void email_with_the_length_more_than_100_should_not_pass_validation() {
            String mockEmail = "test_very_very_very_very_very_very_very_very_very_long_email_address@emailaddressemailaddressemailaddress.com";

            boolean actualResult = userInfoValidator.validateEmail(mockEmail);

            assertFalse(actualResult);
        }

        @Test
        public void null_value_should_not_pass_validation() {
            boolean actualResult = userInfoValidator.validateEmail(null);

            assertFalse(actualResult);
        }
    }

}