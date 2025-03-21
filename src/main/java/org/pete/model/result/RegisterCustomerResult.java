package org.pete.model.result;

import lombok.Getter;

public class RegisterCustomerResult {

    public static class Success extends RegisterCustomerResult {}
    public static class CustAlreadyExists extends RegisterCustomerResult {}

    @Getter
    public static class ValidationFails extends RegisterCustomerResult  {
        private final String message;
        public ValidationFails(String message) {
            this.message = message;
        }
    }
}
