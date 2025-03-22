package org.pete.validator;

import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.regex.Pattern;

@Component
public class UserInfoValidator {

    private static final int MAX_LENGTH = 100;
    private final Pattern pinNumberPattern = Pattern.compile("^\\d{6}$");
    private final Pattern citizenIdPattern = Pattern.compile("^\\d{13}$");
    private final Pattern emailPattern = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");

    public boolean validatePinNumber(String pinNumber) {
        return Objects.nonNull(pinNumber) && pinNumberPattern.matcher(pinNumber).find();
    }

    public boolean validateCitizenId(String citizenId) {
        return Objects.nonNull(citizenId) && citizenIdPattern.matcher(citizenId).find();
    }

    public boolean validateName(String name) {
        return Objects.nonNull(name) && name.length() <= MAX_LENGTH;
    }

    public boolean validateEmail(String email) {
        return Objects.nonNull(email)
                && email.length() <= MAX_LENGTH
                && emailPattern.matcher(email).find();
    }


}
