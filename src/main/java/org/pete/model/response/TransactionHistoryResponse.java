package org.pete.model.response;

import java.util.List;

public record TransactionHistoryResponse(
        List<TransactionHistoryRecord> transactionAuditLogs
) { }
