package org.pete.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CustomerLoginRequest {
    private final String email;
    private final String password;
}
