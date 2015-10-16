package com.erc.dal;

/**
 * Created by einar on 10/3/2015.
 */
public class LogicalOperator {
    private static final String AND = " AND ";
    private static final String OR = " OR ";

    private String operator;

    private LogicalOperator(String operator) {
        this.operator = operator;
    }

    public static LogicalOperator and() {
        return new LogicalOperator(AND);
    }

    public static LogicalOperator or() {
        return new LogicalOperator(OR);
    }

    @Override
    public String toString() {
        return this.operator;
    }
}
