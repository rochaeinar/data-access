package com.erc.dal;

/**
 * Created by einar on 10/3/2015.
 */
public class ExpresionOperator {
    private static final String EQUALS = " = ";
    private static final String IN = " IN ";
    private static final String LIKE = " LIKE ";

    private String operator;

    private ExpresionOperator(String operator) {
        this.operator = operator;
    }

    public static ExpresionOperator equals() {
        return new ExpresionOperator(EQUALS);
    }

    public static ExpresionOperator in() {
        return new ExpresionOperator(IN);
    }

    public static ExpresionOperator like() {
        return new ExpresionOperator(LIKE);
    }

    @Override
    public String toString() {
        return this.operator;
    }
}
