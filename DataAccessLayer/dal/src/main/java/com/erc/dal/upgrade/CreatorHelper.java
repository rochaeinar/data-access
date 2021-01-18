package com.erc.dal.upgrade;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.erc.dal.Log;
import com.erc.dal.QueryBuilder;
import com.erc.dal.SQLiteDatabaseManager;
import com.erc.dal.Table;

public class CreatorHelper {

    public static boolean createTables(DBConfig dbConfig, SQLiteDatabase db) {
        boolean whereCreated = false;
        long numberOfTables = getNumberOfTables(db, dbConfig);
        if (numberOfTables == 0) {
            if (db.isReadOnly()) {
                SQLiteDatabaseManager.open(dbConfig);
            } else {
                String sql = QueryBuilder.getCreateQuery(dbConfig, Table.class);
                Log.w("Database created: " + sql);
                for (String sqlCreate : sql.split(";")) {
                    db.execSQL(sqlCreate);
                }
            }
            whereCreated = true;
        }
        dbConfig.setTableCountCache(numberOfTables);
        return whereCreated;
    }

    private static long getNumberOfTables(SQLiteDatabase db, DBConfig dbConfig) {
        long numberOfTables = 0;
        if (dbConfig.getTableCountCache() != 0) {
            numberOfTables = dbConfig.getTableCountCache();
        } else {
            Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM sqlite_master WHERE type='table' AND NAME <> 'android_metadata';", null);
            if (cursor != null && cursor.moveToNext()) {
                numberOfTables = cursor.getLong(0);
                cursor.close();
            }
        }

        return numberOfTables;
    }
}
