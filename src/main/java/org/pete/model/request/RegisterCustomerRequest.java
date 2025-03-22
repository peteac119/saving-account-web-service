package org.pete.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterCustomerRequest {
    private String thaiName;
    private String englishName;
    private String email;
    private String password;
    private String citizenId;
    private String pinNum;
}
