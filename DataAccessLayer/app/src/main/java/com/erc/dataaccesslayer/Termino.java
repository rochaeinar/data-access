package com.erc.dataaccesslayer;

import com.erc.dal.Entity;
import com.erc.dal.Field;
import com.erc.dal.PrimaryKey;
import com.erc.dal.Table;

/**
 * Created by einar on 8/6/2015.
 */
@Table(name = "Termino")
public class Termino extends Entity {
    @PrimaryKey
    @Field(name = "ID")
    public long id;

    @Field
    public String description;

    @Field
    public int language;

}
