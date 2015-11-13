package com.erc.dal;

/**
 * Created by einar on 10/15/2015.
 */
public class Aggregation {

    public static final String AVG = " AVG(" + Constant.VALUE + ") ";
    public static final String SUM = " SUM(" + Constant.VALUE + ") ";
    public static final String MAX = " MAX(" + Constant.VALUE + ") ";
    public static final String MIN = " MIN(" + Constant.VALUE + ") ";
    public static final String COUNT = " COUNT(" + Constant.VALUE + ") ";

    private String field;
    private String operator;

    private Aggregation(String field, String operator) {
        this.field = field;
        this.operator = operator;
    }

    public static Aggregation avg(String field) {
        return new Aggregation(field, Aggregation.AVG);
    }

    public static Aggregation sum(String field) {
        return new Aggregation(field, Aggregation.SUM);
    }

    public static Aggregation max(String field) {
        return new Aggregation(field, Aggregation.MAX);
    }

    public static Aggregation min(String field) {
        return new Aggregation(field, Aggregation.MIN);
    }

    public static Aggregation count() {
        return new Aggregation("*", Aggregation.COUNT);
    }

    public String getField() {
        return field;
    }

    public String getOperator() {
        return operator;
    }

    public String toString(Class entityClass) {
        String res = "";
        if (entityClass != null) {
            try {
                if (!getField().equals("*")) {
                    String fieldName = ReflectionHelper.getFieldNameFromDBName(entityClass, getField());
                    java.lang.reflect.Field field = entityClass.getField(fieldName);
                }
                res = getOperator().replace(Constant.VALUE, getField());
            } catch (NoSuchFieldException e) {
                Log.e("null field: " + getField(), e);
            }
        } else {
            Log.w("null entityClass on getAggregation");
        }
        return res;
    }
}
