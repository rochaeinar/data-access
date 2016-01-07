package com.erc.dal;

/**
 * Created by einar on 1/4/2016.
 */
public class FieldParam implements ExpresionSide {
    private String field;

    private FieldParam(String field) {
        this.field = field;
    }

    public static FieldParam getInstance(String fieldName) {
        return new FieldParam(fieldName);
    }


    @Override
    public String getString() {
        return this.toString();
    }

    @Override
    public String toString() {
        return field;
    }
}
