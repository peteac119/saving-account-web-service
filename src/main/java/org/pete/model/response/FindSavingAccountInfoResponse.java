package org.pete.model.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record FindSavingAccountInfoResponse(
        String accountNumber,
        BigDecimal currentBalance,
        LocalDateTime creationDate,
        LocalDateTime latestUpdateDate,
        String errorMessage
) {}
