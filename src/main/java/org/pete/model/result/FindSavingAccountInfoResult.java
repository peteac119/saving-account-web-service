package org.pete.model.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class FindSavingAccountInfoResult {
    @Getter
    @AllArgsConstructor
    public static class Success extends FindSavingAccountInfoResult{
        private final String accountNumber;
        private final BigDecimal currentBalance;
        private final LocalDateTime creationDate;
        private final LocalDateTime latestUpdateDate;
    }
    public static class WrongAccountNumber extends FindSavingAccountInfoResult {}
    public static class AccountNotFound extends FindSavingAccountInfoResult {}
}
