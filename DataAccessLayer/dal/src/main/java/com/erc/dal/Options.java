package com.erc.dal;

import android.text.TextUtils;

import java.util.ArrayList;

/**
 * Created by einar on 9/6/2015.
 */
public class Options {


    private String orderBy;
    private String limit;
    private String select;
    private Aggregation aggregation;
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

    public void select(String... fields) {
        if (fields.length > 0) {
            select = TextUtils.join(",", fields);
        }
    }

    public void limit(int limit) {
        this.limit = limit + "";
    }

    public void avg(String field) {
        clearForAggregation();
        aggregation = new Aggregation(field, Aggregation.AVG);
    }

    public void sum(String field) {
        clearForAggregation();
        aggregation = new Aggregation(field, Aggregation.SUM);
    }

    public void max(String field) {
        clearForAggregation();
        aggregation = new Aggregation(field, Aggregation.MAX);
    }

    public void min(String field) {
        clearForAggregation();
        aggregation = new Aggregation(field, Aggregation.MIN);
    }

    public void count() {
        clearForAggregation();
        aggregation = new Aggregation("*", Aggregation.COUNT);
    }

    public String getSql(Entity entity, String selectAllSQL) {
        this.entity = entity;
        tableName = QueryBuilder.geTableName(entity.getClass());

        StringBuffer sb = new StringBuffer();

        if (getDistinct()) {
            selectAllSQL = selectAllSQL.replace(Ctt.SELECT, Ctt.SELECT + Ctt.DISTINCT);
        }
        if (!Util.isNullOrEmpty(getSelect())) {
            selectAllSQL = selectAllSQL.replace("*", getSelect());
        }
        if (!Util.isNullOrEmpty(getAggregation())) {
            selectAllSQL = selectAllSQL.replace("*", getAggregation());
        }
        sb.append(selectAllSQL);
        String expresions = getExpressions();
        if (expresions.length() > 0) {
            sb.append(Ctt.WHERE);
            sb.append(expresions);
        }
        sb.append(getOrderBy());
        sb.append(getLimit());

        return sb.toString();
    }

    private String getExpressions() {
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

    private String getOrderBy() {
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

    private boolean getDistinct() {
        return distinct;
    }

    private String getLimit() {
        String res = "";
        if (!Util.isNullOrEmpty(limit))
            res = Ctt.LIMIT + limit;
        return res;
    }

    private String getSelect() {
        return select;
    }

    private String getAggregation() {
        String res = "";
        if (aggregation != null) {
            if (entity != null) {
                try {
                    if (!aggregation.getField().equals("*")) {
                        java.lang.reflect.Field field = entity.getClass().getField(aggregation.getField());
                    }
                    res = aggregation.getOperator().replace(Ctt.VALUE, aggregation.getField());
                } catch (NoSuchFieldException e) {
                    Log.e("null field: " + aggregation.getField(), e);
                }
            } else {
                Log.w("null entity on getAggregation");
            }
        }
        return res;
    }

    private void clearForAggregation() {
        if (!Util.isNullOrEmpty(select)) {
            Log.w("Fields to select '" + select + "' will be ignored... in order to use aggregation operator");
        }
        if (distinct) {
            Log.w("'Distinct' will be ignored... in order to use aggregation operator");
        }
        select = null;
        distinct = false;

        if (aggregation != null) {
            Log.w("Previous aggregation operator '" + aggregation.getOperator().replace(Ctt.VALUE, aggregation.getField()) + "' will be ignored");
        }
    }
}
