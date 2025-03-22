package org.pete.model.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

public class TransferResult {
    @Getter
    @AllArgsConstructor
    public static class Success extends TransferResult {
        private final String senderAccountNumber;
        private final String beneficiaryAccountNumber;
        private final BigDecimal currentSenderBalance;
        private final BigDecimal currentBeneficiaryBalance;
    }

    @Getter
    @AllArgsConstructor
    public static class NotEnoughBalance extends TransferResult {
        private final String senderAccountNumber;
        private final BigDecimal currentSenderBalance;
    }

    @Getter
    @AllArgsConstructor
    public static class SavingAccountNotFound extends TransferResult {
        private final String message;
    }

    public static class NotPinNumberProvided extends TransferResult {}
    public static class WrongPinNumber extends TransferResult {}
    public static class TransferAmountIsLessThanOne extends TransferResult {}
    public static class WrongSenderAccount extends TransferResult {}
    public static class SameAccountNumber extends TransferResult {}

    public static String getErrorMessageFromResultType(TransferResult result) {
        return switch (result) {
            case NotPinNumberProvided notPinNumberProvided -> "Pin number is not provided";
            case WrongPinNumber wrongPinNumber -> "Pin number is not matched";
            case TransferAmountIsLessThanOne transferAmountIsLessThanOne -> "Transfer amount must be more than one";
            case WrongSenderAccount wrongSenderAccount -> "Wrong sender account";
            case SameAccountNumber sameAccountNumber -> "Both sender and beneficiary accounts are the same";
            default -> null;
        };
    }
}
