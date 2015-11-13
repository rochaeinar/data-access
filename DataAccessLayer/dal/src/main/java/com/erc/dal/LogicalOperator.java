package com.erc.dal;

/**
 * Created by einar on 10/3/2015.
 */
public enum LogicalOperator {
    AND(" AND "),
    OR(" OR ");

    private String operator;

    private LogicalOperator(String operator) {
        this.operator = operator;
    }

    @Override
    public String toString() {
        return operator;
    }
}
