package com.erc.dataaccesslayer;

import com.erc.dal.Entity;
import com.erc.dal.Field;
import com.erc.dal.PrimaryKey;
import com.erc.dal.Table;

import java.util.Date;

/**
 * Created by einar on 8/6/2015.
 */
@Table(name = "Marcador")
public class Marcador extends Entity {
    @PrimaryKey
    @Field(name = "ID")
    public long id;

    @Field
    public String description;

    @Field
    public char code;

    @Field
    public int language;

    @Field
    public boolean status;

    @Field
    public Date date;

    @Field
    public short myShort;

    @Field
    public double myDouble;

    @Field
    public float myFloat;


    public Marcador() {
        super();
        date = new Date();
        status = true;

    }

    @Override
    public String toString() {
        return "Marcador{" +
                "id=" + id +
                ", description='" + description + '\'' +
                ", code=" + code +
                ", language=" + language +
                ", status=" + status +
                ", date=" + date +
                ", myShort=" + myShort +
                ", myDouble=" + myDouble +
                ", myFloat=" + myFloat +
                '}';
    }
}
