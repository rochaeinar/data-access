package com.erc.dal;

import android.content.Context;

import com.erc.dal.upgrade.DBConfig;

import java.util.HashMap;

/**
 * Created by Einar on 7/9/2018.
 */

public class DBs {
    private HashMap<String, DB> dbsMap;
    private static DBs dbs = null;

    public static DBs getInstance() {

        if (dbs == null) {
            dbs = new DBs();
        }

        return dbs;
    }

    public DB getDB(DBConfig dbConfig) {
        String fullDatabaseName = getFullDatabaseName(dbConfig);
        if (!dbsMap.containsKey(fullDatabaseName)) {
            dbsMap.put(fullDatabaseName, new DB(dbConfig));
        }
        return dbsMap.get(fullDatabaseName);
    }

    public void removeDB(DBConfig dbConfig) {
        String fullDatabaseName = getFullDatabaseName(dbConfig);
        if (dbsMap.containsKey(fullDatabaseName)) {
            dbsMap.remove(fullDatabaseName);
        }
    }

    private DBs() {
        dbsMap = new HashMap<>();
    }

    private static String getFullDatabaseName(DBConfig dbConfig) {
        return dbConfig.getUrl() + "/" + dbConfig.getDataBaseName();
    }
}
