package com.erc.dal;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.erc.dal.upgrade.CreatorHelper;
import com.erc.dal.upgrade.DBConfig;
import com.erc.dal.upgrade.UpgradeHelper;

import java.io.File;

public class SQLiteDatabaseManager extends SQLiteOpenHelper {

    private final UpgradeHelper upgradeHelper;

    public SQLiteDatabaseManager(DBConfig dbConfig) {
        super(dbConfig.getContext(), dbConfig.getDataBaseName(), null, dbConfig.getVersion());
        upgradeHelper = new UpgradeHelper();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public synchronized SQLiteDatabase open(DBConfig dbConfig, SQLiteDatabase db) {
        try {
            if (Util.isNullOrEmpty(dbConfig.getUrl())) {
                db = getWritableDatabase();
                db.setLockingEnabled(false);
            } else {
                createDirectory(dbConfig);
                db = SQLiteDatabase.openDatabase(getFullDatabaseName(dbConfig), null, SQLiteDatabase.OPEN_READWRITE | SQLiteDatabase.NO_LOCALIZED_COLLATORS | SQLiteDatabase.CREATE_IF_NECESSARY);
            }
            if (CreatorHelper.createTables(dbConfig, db, this)) {
                return open(dbConfig, db);
            }
            upgradeHelper.verifyUpgrade(dbConfig, db, this);
        } catch (Exception e) {
            Log.e("Opening database", e);
        }
        return db;
    }

    private static void createDirectory(DBConfig dbConfig) {
        File dir = new File(dbConfig.getUrl());
        if (!dir.exists()) {
            dir.mkdirs();
            dbConfig.clearCache();
        }
        File databaseFile = new File(dbConfig.getFullDatabaseName());
        if (!databaseFile.exists()) {
            dbConfig.clearCache();
        }
    }

    public synchronized SQLiteDatabase openReadOnly(DBConfig dbConfig, SQLiteDatabase db) {
        try {
            if (Util.isNullOrEmpty(dbConfig.getUrl())) {
                db = getReadableDatabase();
                db.setLockingEnabled(false);
            } else {
                db = SQLiteDatabase.openDatabase(getFullDatabaseName(dbConfig), null, SQLiteDatabase.OPEN_READONLY | SQLiteDatabase.NO_LOCALIZED_COLLATORS | SQLiteDatabase.CREATE_IF_NECESSARY);
            }
            if (CreatorHelper.createTables(dbConfig, db, this)) {
                return openReadOnly(dbConfig, db);
            }
            upgradeHelper.verifyUpgrade(dbConfig, db, this);
        } catch (Exception e) {
            Log.e("Opening database", e);
        }
        return db;
    }

    public static String getDataBaseName(Context context) {
        String dataBaseName = RegEx.match(context.getPackageName(), RegEx.PATTERN_EXTENSION);
        dataBaseName = dataBaseName.replace(".", "");
        return dataBaseName;
    }

    private static String getFullDatabaseName(DBConfig dbConfig) {
        return dbConfig.getUrl() + "/" + dbConfig.getDataBaseName();
    }
}
