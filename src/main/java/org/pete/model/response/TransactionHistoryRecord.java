package org.pete.model.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public record TransactionHistoryRecord(
        LocalDate transactionDate,
        LocalTime transactionTime,
        String code,
        String channel,
        BigDecimal transactionAmount,
        BigDecimal balance,
        String remarks
) {}
