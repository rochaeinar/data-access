package com.erc.dal;

import java.util.Date;

/**
 * Created by einar on 9/6/2015.
 */
public class HelperDataType {
    public static final String INTEGER = "INTEGER";
    public static final String TEXT = "TEXT";
    public static final String REAL = "REAL";

    public static String getDataBaseType(Class type) {
        String res = TEXT;
        do {
            if (type.equals(String.class)) {
                break;
            }
            if (type.equals(char.class) || type.equals(Character.class)) {
                break;
            }
            if (type.equals(Date.class)) {
                break;
            }

            if (type.equals(short.class) || type.equals(Short.class)) {
                res = INTEGER;
                break;
            }
            if (type.equals(int.class) || type.equals(Integer.class)) {
                res = INTEGER;
                break;
            }
            if (type.equals(long.class) || type.equals(Long.class)) {
                res = INTEGER;
                break;
            }
            if (type.equals(boolean.class) || type.equals(Boolean.class)) {
                res = INTEGER;
                break;
            }

            if (type.equals(double.class) || type.equals(Double.class)) {
                res = REAL;
                break;
            }
            if (type.equals(float.class) || type.equals(Float.class)) {
                res = REAL;
                break;
            }

        }
        while (false);
        return res;
    }

    public static boolean hasCuotes(Object value) {
        return getDataBaseType(value.getClass()).equals(TEXT);
    }

    public static boolean hasCuotes(Class type) {
        return getDataBaseType(type).equals(TEXT);
    }
}
