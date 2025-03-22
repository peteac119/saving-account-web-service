package org.pete.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransferRequest {
    private String senderAccountNum;
    private String beneficiaryAccountNum;
    private BigDecimal transferAmount;
    private String pinNumber;
}
