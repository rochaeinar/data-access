package com.erc.dal;

import android.content.Context;
import android.database.Cursor;

import com.erc.dal.upgrade.DBConfig;

import java.util.ArrayList;

/**
 * Created by einar on 20-10-15.
 */
public class DB {
    private DBConfig dbConfig;
    private DBOperations dbOperations;

    public DB(Context context) {
        this.dbConfig = new DBConfig(context, null, 1, null);
        dbOperations = new DBOperations(dbConfig);
        dbOperations.initialize();
    }

    public void initialize() {
        dbOperations.initialize();
    }

    public DB(DBConfig dbConfig) {
        this.dbConfig = dbConfig;
        dbOperations = new DBOperations(dbConfig);
        dbOperations.initialize();
    }

    public Entity save(Entity entity) {
        return dbOperations.save(entity, this.dbConfig);
    }

    public <T> T getById(Class classType, Object id) {
        return dbOperations.getById(classType, id, this.dbConfig);
    }

    public <T> ArrayList<T> getAll(Class classType, Options... options) {
        return dbOperations.getAll(classType, this.dbConfig, options);
    }

    public <T> T calculate(Class classType, Aggregation aggregationOperator, Options... options) {
        return dbOperations.calculate(classType, aggregationOperator, this.dbConfig, options);
    }

    public boolean remove(Class classType, Object id) {
        return dbOperations.remove(classType, id, this.dbConfig);
    }

    public boolean execSQL(String sql) {
        return dbOperations.execSQL(sql, this.dbConfig);
    }

    public Cursor rawQuery(String sql) {
        return dbOperations.rawQuery(sql, this.dbConfig);
    }
}
