package org.pete.model.response;

import java.math.BigDecimal;

public record DepositResponse(
        String accountNumber,
        BigDecimal newBalance,
        String errorMessage
)
{ }
