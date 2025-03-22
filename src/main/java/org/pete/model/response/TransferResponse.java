package org.pete.model.response;

import java.math.BigDecimal;

public record TransferResponse(
        String senderAccountNumber,
        String beneficiaryAccountNumber,
        BigDecimal currentSenderBalance,
        BigDecimal currentBeneficiaryBalance,
        String errorMessage
)
{}
