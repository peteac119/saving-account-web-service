package org.pete.model.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

public class DepositResult {
    public static class SavingAccountNotFound extends DepositResult {}
    public static class DepositAmountIsLessThanOne extends DepositResult {}

    @Getter
    @AllArgsConstructor
    public static class Success extends DepositResult {
        private final String accountNumber;
        private final BigDecimal newBalance;
    }
}
