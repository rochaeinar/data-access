package com.erc.dal.upgrade;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.erc.dal.SQLiteDatabaseManager;

public class UpgradeHelper {

    public void verifyUpgrade(DBConfig dbConfig, SQLiteDatabase db, SQLiteDatabaseManager sqLiteDatabaseManager) {
        if (dbConfig.getUpgradeListener() != null) {
            int currentVersion = getCurrentVersion(db, dbConfig);

            if (dbConfig.getVersion() != currentVersion) {
                if (db.isReadOnly()) {
                    db = sqLiteDatabaseManager.open(dbConfig, db);
                } else {
                    createMetadataStructure(db);
                    dbConfig.getUpgradeListener().onUpgrade(db, currentVersion, dbConfig.getVersion());
                    updateVersion(db, dbConfig.getVersion());
                }
            }
            dbConfig.setCurrentVersionCache(dbConfig.getVersion());
        }
    }

    private int getCurrentVersion(SQLiteDatabase db, DBConfig dbConfig) {

        if (dbConfig.getCurrentVersionCache() != 0) {
            return dbConfig.getCurrentVersionCache();
        }

        boolean tableExist = false;
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM sqlite_master WHERE type='table' AND name='METADATA';", null);
        if (cursor != null && cursor.moveToNext()) {
            tableExist = cursor.getLong(0) > 0;
            if (!cursor.isClosed()) {
                cursor.close();
            }
        }

        if (tableExist) {
            cursor = db.rawQuery("SELECT * FROM METADATA", null);
            if (cursor != null && cursor.moveToNext()) {
                int version = Integer.parseInt(cursor.getString(cursor.getColumnIndex("VALUE")));
                cursor.close();
                return version;
            }
        }

        return -1;
    }

    private void createMetadataStructure(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS METADATA (ID INTEGER, NAME TEXT, VALUE TEXT)");
    }

    private void updateVersion(SQLiteDatabase db, int version) {
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM METADATA", null);
        long count = 0;
        if (cursor != null && cursor.moveToNext()) {
            count = cursor.getLong(0);
            if (!cursor.isClosed()) {
                cursor.close();
            }
        }

        if (count == 0) {
            db.execSQL("INSERT INTO METADATA (ID, NAME, VALUE) VALUES ( 1, 'CURRENT_VERSION_DB', '" + version + "');");
        } else {
            db.execSQL("UPDATE METADATA SET VALUE = '" + version + "' WHERE ID = '1'");
        }
    }
}
