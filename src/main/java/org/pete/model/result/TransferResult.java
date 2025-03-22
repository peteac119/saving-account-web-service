package org.pete.model.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import java.math.BigDecimal;

@Data
public class TransferResult {
    @Getter
    @AllArgsConstructor
    private static class Success extends TransferResult {
        private final String senderAccountNumber;
        private final String beneficiaryAccountNumber;
        private final BigDecimal currentSenderBalance;
        private final BigDecimal currentBeneficiaryBalance;
    }

    @Getter
    @AllArgsConstructor
    private static class NotEnoughBalance extends TransferResult {
        private final String senderAccountNumber;
        private final BigDecimal currentSenderBalance;
    }

    @Getter
    @AllArgsConstructor
    private static class SavingAccountNotFound extends TransferResult {
        private final String message;
    }

    private static class TransferAmountIsLessThan extends TransferResult {}
}
