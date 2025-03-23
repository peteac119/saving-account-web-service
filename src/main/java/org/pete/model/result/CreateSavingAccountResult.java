package org.pete.model.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

public class CreateSavingAccountResult {

    @Getter
    @AllArgsConstructor
    public static class Success extends CreateSavingAccountResult {
        private final String accountNumber;
        private final BigDecimal currentBalance;
    }

    public static class AmountIsNegative extends CreateSavingAccountResult {}
    public static class CustNotFound extends CreateSavingAccountResult {}
}
