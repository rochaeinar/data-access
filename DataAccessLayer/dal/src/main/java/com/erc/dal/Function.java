package com.erc.dal;

import android.text.TextUtils;

/**
 * Created by einar on 1/4/2016.
 */
public class Function implements ExpresionSide {

    @Override
    public String getString() {
        return this.toString();
    }

    private static enum FunctionName {
        LOWER,
        UPPER,
        REPLACE
    }

    private FunctionName functionName;
    private Object[] params;
    private Class returnType;

    private Function(FunctionName functionName, Object[] params, Class returnType) {
        this.functionName = functionName;
        this.params = params;
        this.returnType = returnType;
    }

    /**
     * Use this factory method to create a new instance of
     * this Function using the provided parameters.
     *
     * @param params ONE Parameter: text to be lowered
     * @return A new instance of Function lower.
     */
    public static Function lower(Object... params) {
        return new Function(FunctionName.LOWER, params, String.class);
    }

    /**
     * Use this factory method to create a new instance of
     * this Function using the provided parameters.
     *
     * @param params ONE Parameter: text to be lowered
     * @return A new instance of Function lower.
     */
    public static Function upper(Object... params) {
        return new Function(FunctionName.UPPER, params, String.class);
    }

    /**
     * Use this factory method to create a new instance of
     * this Function using the provided parameters.
     *
     * @param params THREE Parameter X, Y, Z: function returns a string formed by substituting string Z for every occurrence of string Y in string X
     * @return A new instance of Function replace.
     */
    public static Function replace(Object... params) {
        return new Function(FunctionName.REPLACE, params, String.class);
    }


    public FunctionName getFunctionName() {
        return functionName;
    }

    public Object[] getParams() {
        return params;
    }

    public Class getReturnType() {
        return returnType;
    }

    @Override
    public String toString() {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(this.functionName.name());
        stringBuffer.append("(");
        for (int i = 0; i < params.length; i++) {
            boolean hasQuotes = params[i] instanceof String;
            String quotes = hasQuotes ? "'" : "";
            stringBuffer.append(quotes + params[i].toString() + quotes);

            if (i < params.length - 1) {
                stringBuffer.append(",");
            }
        }
        stringBuffer.append(")");

        return stringBuffer.toString();
    }
}
