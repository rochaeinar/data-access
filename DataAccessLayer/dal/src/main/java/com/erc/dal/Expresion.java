package com.erc.dal;

/**
 * Created by einar on 10/11/2015.
 */
public class Expresion {

    private ExpresionOperator expresionOperator;
    private LogicalOperator logicalOperator;
    private String left;
    private String right;
    private boolean ignoreQuotes;

    public Expresion(String left, ExpresionOperator expresionOperator, String right, LogicalOperator logicalOperator) {
        this.expresionOperator = expresionOperator;
        this.logicalOperator = logicalOperator;
        this.left = left;
        this.right = right;
        ignoreQuotes = false;
    }

    public LogicalOperator getLogicalOperator() {
        return logicalOperator;
    }

    public String getLeft() {
        return left;
    }

    public boolean isIgnoreQuotes() {
        return ignoreQuotes;
    }

    public void setIgnoreQuotes(boolean ignoreQuotes) {
        this.ignoreQuotes = ignoreQuotes;
    }

    public String getExpresionString(String table, boolean hasCuotes) {
        String value = hasCuotes && !isIgnoreQuotes() ? Constant.VALUE_QUOTES : Constant.VALUE;
        return table + "." + left + expresionOperator + value.replace(Constant.VALUE, right);
    }
}
