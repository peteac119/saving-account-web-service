package org.pete.constant;

import lombok.Getter;

@Getter
public enum Channel {
    TELLER("OTC"), CUSTOMER("ATS");

    private final String code;

    Channel(String code) {
        this.code = code;
    }
}
