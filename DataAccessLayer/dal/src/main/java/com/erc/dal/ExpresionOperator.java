package com.erc.dal;

/**
 * Created by einar on 10/3/2015.
 */
public enum ExpresionOperator {
    EQUALS(" = "),
    IN(" IN "),
    LIKE(" LIKE ");

    private String operator;

    private ExpresionOperator(String operator) {
        this.operator = operator;
    }

    @Override
    public String toString() {
        return this.operator;
    }
}
