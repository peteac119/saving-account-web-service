package org.pete.model.response;

import java.math.BigDecimal;

public record CreateSavingAccountResponse(
        String accountNumber,
        BigDecimal currentBalance,
        String errorMessage
) {}
