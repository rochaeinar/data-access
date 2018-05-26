package com.erc.dal;

import java.util.ArrayList;

/**
 * Created by einar on 14/5/2018.
 */
public class Group extends Sentence {

    private LogicalOperator logicalOperator;
    private Options options;
    private Options parent;
    private ArrayList<Expresion> expressions;


    public Group(Options parent) {
        this.expressions = new ArrayList<Expresion>();
        this.options = new Options();
        this.parent = parent;
    }

    public void and(String fieldName, Object value, ExpresionOperator... expresionOperator) {
        options.and(FieldParam.getInstance(fieldName), value, expresionOperator);
    }

    public void and(ExpresionSide fieldName, Object value, ExpresionOperator... expresionOperator) {
        options.and(fieldName, value, expresionOperator);
    }

    public void or(String fieldName, Object value, ExpresionOperator... expresionOperator) {
        options.or(FieldParam.getInstance(fieldName), value, expresionOperator);
    }

    public void or(ExpresionSide fieldName, Object value, ExpresionOperator... expresionOperator) {
        options.or(fieldName, value, expresionOperator);
    }

    public void in(String fieldName, ArrayList values, LogicalOperator... logicalOperator) {
        options.in(FieldParam.getInstance(fieldName), values, logicalOperator);
    }

    public void in(ExpresionSide fieldName, ArrayList values, LogicalOperator... logicalOperator) {
        options.in(fieldName, values, logicalOperator);
    }

    public String getExpressions() {
        options.tableName = parent.tableName;
        options.entityClass = parent.entityClass;
        return options.getExpressions();
    }

    public LogicalOperator getLogicalOperator() {
        return logicalOperator;
    }

    public void setLogicalOperator(LogicalOperator logicalOperator) {
        this.logicalOperator = logicalOperator;
    }
}
