package org.pete.model.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

public class RegisterCustomerResult {

    public static class Success extends RegisterCustomerResult {}
    public static class CustAlreadyExists extends RegisterCustomerResult {}

    @Getter
    @AllArgsConstructor
    public static class ValidationFails extends RegisterCustomerResult  {
        private final String message;
    }
}
