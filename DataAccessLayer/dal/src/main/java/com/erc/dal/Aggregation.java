package com.erc.dal;

/**
 * Created by einar on 10/15/2015.
 */
public class Aggregation {

    public static final String AVG = " AVG(" + Ctt.VALUE + ") ";
    public static final String SUM = " SUM(" + Ctt.VALUE + ") ";
    public static final String MAX = " MAX(" + Ctt.VALUE + ") ";
    public static final String MIN = " MIN(" + Ctt.VALUE + ") ";
    public static final String COUNT = " COUNT(" + Ctt.VALUE + ") ";

    private String field;
    private String operator;

    public Aggregation(String field, String operator) {
        this.field = field;
        this.operator = operator;
    }

    public String getField() {
        return field;
    }

    public String getOperator() {
        return operator;
    }
}
