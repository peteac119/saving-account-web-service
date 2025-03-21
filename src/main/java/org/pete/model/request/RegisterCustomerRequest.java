package org.pete.model.request;

import lombok.Data;

@Data
public class RegisterCustomerRequest {
    private String thaiName;
    private String englishName;
    private String email;
    private String password;
    private String citizenId;
    private String pinNum;
}
