package org.pete.constant;

public enum Channel {
    TELLER("OTC"), CUSTOMER("ATS");

    private final String code;

    Channel(String code) {
        this.code = code;
    }

    public String getCode() {
        return this.code;
    }
}
