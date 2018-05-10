package com.erc.dal;

/**
 * Created by einar on 10/3/2015.
 */
public enum ExpresionOperator {
    EQUALS(" = "),
    GREATER_THAN(" > "),
    LESS_THAN(" < "),
    GREATER_THAN_OR_EQUAL_TO(" >= "),
    LESS_THAN_OR_EQUAL_TO(" <= "),
    NOT_EQUAL_TO(" <> "),
    NOT_LESS_THAN(" !< "),
    NOT_EQUAL_TO_DIFF(" != "),
    NOT_GREATER_THAN(" !> "),
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
