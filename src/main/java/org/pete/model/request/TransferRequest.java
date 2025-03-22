package org.pete.model.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransferRequest {
    private String senderAccountNum;
    private String beneficiaryAccountNum;
    private BigDecimal transferAmount;
    private String pinNumber;
}
