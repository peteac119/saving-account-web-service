package org.pete.constant;

public enum TransactionAction {
    DEPOSIT("DP"), TRANSFER("TF");

    private final String code;

    TransactionAction(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
