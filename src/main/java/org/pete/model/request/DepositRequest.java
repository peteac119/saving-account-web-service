package org.pete.model.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DepositRequest {
    private BigDecimal depositAmount;
    private String accountNumber;
}
