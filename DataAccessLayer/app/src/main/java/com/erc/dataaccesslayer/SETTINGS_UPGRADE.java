package com.erc.dataaccesslayer;

import com.erc.dal.Entity;
import com.erc.dal.Field;
import com.erc.dal.PrimaryKey;
import com.erc.dal.Table;

/**
 * Created by einar on 12/30/2016.
 */
@Table(name = "SETTINGS")
public class SETTINGS_UPGRADE extends Entity {

    @PrimaryKey
    @Field
    public long ID;

    @Field
    public String NAME;

    @Field
    public String VALUE;

    @Field
    public float UPGRADED_VALUE;


    @Override
    public String toString() {
        return "SETTINGS{" +
                "ID=" + ID +
                ", NAME='" + NAME + '\'' +
                ", VALUE='" + VALUE + '\'' +
                '}';
    }
}
