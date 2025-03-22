package org.pete.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateSavingAccountRequest {
    private String thaiName;
    private String englishName;
    private String citizenId;
    private BigDecimal depositAmount;
}
