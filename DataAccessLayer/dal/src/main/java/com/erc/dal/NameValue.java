package com.erc.dal;

/**
 * Created by einar on 9/5/2015.
 */
public class NameValue {
    private Object name;
    private Object value;

    public NameValue(Object name, Object value) {
        this.name = name;
        this.value = value;
    }

    public Object getName() {
        return name;
    }

    public void setName(Object name) {
        this.name = name;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}
