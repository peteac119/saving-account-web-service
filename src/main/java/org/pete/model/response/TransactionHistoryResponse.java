package org.pete.model.response;

import org.pete.entity.TransactionAuditLog;

import java.util.List;

public record TransactionHistoryResponse(
        List<TransactionAuditLog> transactionAuditLogs
) { }
