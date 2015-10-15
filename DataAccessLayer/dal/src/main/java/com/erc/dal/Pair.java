package com.erc.dal;

/**
 * Created by einar on 9/5/2015.
 */
public class Pair {
    private Class type;
    private String name;
    private String value;

    public Pair() {
    }

    public Pair(Class type, String name, String value) {
        this.type = type;
        this.name = name;
        this.value = value;
    }

    public Class getType() {
        return type;
    }

    public void setType(Class type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        boolean hasQuotes = type.equals(String.class);
        String res = "";
        if (hasQuotes) {
            res = Ctt.TABLE_PAIR_QUOTES.replaceFirst(Ctt.FIELD, name).replaceFirst(Ctt.VALUE, value);
        } else {
            res = Ctt.TABLE_PAIR.replaceFirst(Ctt.FIELD, name).replaceFirst(Ctt.VALUE, value);
        }
        return res;
    }
}
