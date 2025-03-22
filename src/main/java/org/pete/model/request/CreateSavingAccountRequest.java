package org.pete.model.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateSavingAccountRequest {
    private String thaiName;
    private String englishName;
    private String citizenId;
    private BigDecimal depositAmount;
}
