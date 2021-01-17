package com.erc.dataaccesslayer;

import com.erc.dal.Entity;
import com.erc.dal.Field;
import com.erc.dal.PrimaryKey;
import com.erc.dal.Table;

/**
 * Created by einar on 12/30/2016.
 */
@Table
public class SETTINGS extends Entity {

    @PrimaryKey
    @Field
    public long ID;

    @Field
    public String NAME;

    @Field
    public String VALUE;

    @Override
    public String toString() {
        return "SETTINGS{" +
                "ID=" + ID +
                ", NAME='" + NAME + '\'' +
                ", VALUE='" + VALUE + '\'' +
                '}';
    }
}
