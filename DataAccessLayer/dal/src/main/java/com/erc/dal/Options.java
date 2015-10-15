package com.erc.dal;

import android.text.TextUtils;

import java.util.ArrayList;

/**
 * Created by einar on 9/6/2015.
 */
public class Options {


    private String orderBy;
    private boolean ascending;
    private boolean distinct;
    private String count;
    private String min;
    private String max;
    private ArrayList<Expresion> expresions;
    private Entity entity;
    String tableName;


    public Options() {
        expresions = new ArrayList<Expresion>();
    }

    public void setEntity(Entity entity) {
        this.entity = entity;
        tableName = QueryBuilder.geTableName(entity.getClass());
    }

    public String getExpressions() {
        StringBuffer sb = new StringBuffer();
        if (!Util.isNullOrEmpty(tableName)) {
            int i = 0;
            for (Expresion e : expresions) {
                if (i > 0) {
                    sb.append(e.getLogicalOperator());
                }
                try {
                    Class type = entity.getClass().getField(e.getLeft()).getType();
                    sb.append(e.getExpresionString(tableName, HelperDataType.hasCuotes(type)));
                    i++;
                } catch (NoSuchFieldException e1) {
                    Log.e("Invalid Expresion on  getExpressions", e1);
                }
            }
        } else {
            Log.w("Null TableName on getExpressions");
        }
        return sb.toString();
    }

    public String getOrderBy() {
        String res = "";
        if (!Util.isNullOrEmpty(tableName)) {
            if (!Util.isNullOrEmpty(orderBy)) {
                res = Ctt.ORDER_BY + tableName + "." + orderBy + (ascending ? Ctt.ASC : Ctt.DESC);
            }
        } else {
            Log.w("Null TableName on getOrderBy");
        }
        return res;
    }

    public void and(String fieldName, String value, ExpresionOperator... expresionOperator) {
        if (!Util.isNullOrEmpty(fieldName) && !Util.isNullOrEmpty(value)) {
            ExpresionOperator expresionOperator_ = expresionOperator.length == 0 ? ExpresionOperator.equals() : expresionOperator[0];
            expresions.add(new Expresion(fieldName, expresionOperator_, value, LogicalOperator.and()));
        } else {
            Log.w("Null or empty value on Options.and: " + fieldName + ", " + value);
        }
    }

    public void or(String fieldName, String value, ExpresionOperator... expresionOperator) {
        if (!Util.isNullOrEmpty(fieldName) && !Util.isNullOrEmpty(value)) {
            ExpresionOperator expresionOperator_ = expresionOperator.length == 0 ? ExpresionOperator.equals() : expresionOperator[0];
            expresions.add(new Expresion(fieldName, expresionOperator_, value, LogicalOperator.or()));
        } else {
            Log.w("Null or empty value on Options.or: " + fieldName + ", " + value);
        }
    }

    public void orderBy(String fieldName, boolean ascending) {
        this.orderBy = fieldName;
        this.ascending = ascending;
    }

    public void distinct(boolean distinct) {
        this.distinct = distinct;
    }

    public boolean getDistinct() {
        return distinct;
    }

    public void in(String fieldName, ArrayList values, LogicalOperator... logicalOperator) {
        LogicalOperator logicalOperator_ = logicalOperator.length == 0 ? LogicalOperator.and() : logicalOperator[0];
        ArrayList<String> items = new ArrayList<>();
        if (values.size() > 0) {
            boolean hasQuotes = HelperDataType.hasCuotes(values.get(0));
            for (Object item : values) {
                items.add((hasQuotes ? Ctt.VALUE_QUOTES : Ctt.VALUE).replaceFirst(Ctt.VALUE, Util.getValueFromObject(item)));
            }
            Expresion expresion = new Expresion(fieldName, ExpresionOperator.in(), "(" + TextUtils.join(",", items) + ")", logicalOperator_);
            expresion.setIgnoreQuotes(true);
            expresions.add(expresion);
        }
    }

    public boolean getIn() {
        return distinct;
    }

    public void select(String... fields) {

    }

    public void limit(int limit) {

    }

}
