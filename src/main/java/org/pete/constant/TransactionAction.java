package org.pete.constant;

import lombok.Getter;

@Getter
public enum TransactionAction {
    DEPOSIT("DP"), TRANSFER("TF");

    private final String code;

    TransactionAction(String code) {
        this.code = code;
    }
}
