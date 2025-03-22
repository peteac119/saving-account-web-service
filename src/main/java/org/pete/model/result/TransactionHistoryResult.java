package org.pete.model.result;


import lombok.AllArgsConstructor;
import lombok.Getter;
import org.pete.entity.TransactionAuditLog;

import java.util.List;

public class TransactionHistoryResult {
    @Getter
    @AllArgsConstructor
    public static class Success extends TransactionHistoryResult {
        private final List<TransactionAuditLog> transactionAuditLogs;
    }

    public static class AccountNotFound extends TransactionHistoryResult {}
    public static class WrongAccountNumber extends TransactionHistoryResult {}
}
