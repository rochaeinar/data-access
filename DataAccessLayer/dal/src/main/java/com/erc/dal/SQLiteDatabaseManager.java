package com.erc.dal;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.erc.dal.upgrade.CreatorHelper;
import com.erc.dal.upgrade.DBConfig;
import com.erc.dal.upgrade.UpgradeHelper;
import com.erc.dal.upgrade.UpgradeListener;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by einar on 9/6/2015.
 */
public class SQLiteDatabaseManager extends SQLiteOpenHelper {

    private static Map<String, UpgradeListener> upgradeableMap = new HashMap<>();
    private static SQLiteDatabaseManager sqLiteDatabaseManager;
    private Context context;
    private DBConfig dbConfig;

    private SQLiteDatabaseManager(DBConfig dbConfigs) {
        super(dbConfigs.getContext(), dbConfigs.getDataBaseName(), null, dbConfigs.getVersion());
        this.dbConfig = dbConfigs;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    private static SQLiteDatabaseManager getInstance(DBConfig dbConfig) {
        if (sqLiteDatabaseManager == null) {
            sqLiteDatabaseManager = new SQLiteDatabaseManager(dbConfig);
        }
        return sqLiteDatabaseManager;
    }

    public synchronized static SQLiteDatabase open(DBConfig dbConfig, SQLiteDatabase db) {
        try {
            if (Util.isNullOrEmpty(dbConfig.getUrl())) {
                db = getInstance(dbConfig).getWritableDatabase();
                db.setLockingEnabled(false);
            } else {
                db = SQLiteDatabase.openDatabase(getFullDatabaseName(dbConfig), null, SQLiteDatabase.OPEN_READWRITE | SQLiteDatabase.NO_LOCALIZED_COLLATORS | SQLiteDatabase.CREATE_IF_NECESSARY);
            }
            if (CreatorHelper.createTables(dbConfig, db)) {
                return open(dbConfig, db);
            }
            UpgradeHelper.verifyUpgrade(dbConfig, db);
        } catch (Exception e) {
            Log.e("Opening database", e);
        }
        return db;
    }

    public synchronized static SQLiteDatabase openReadOnly(DBConfig dbConfig, SQLiteDatabase db) {
        try {
            if (Util.isNullOrEmpty(dbConfig.getUrl())) {
                db = getInstance(dbConfig).getReadableDatabase();
                db.setLockingEnabled(false);
            } else {
                db = SQLiteDatabase.openDatabase(getFullDatabaseName(dbConfig), null, SQLiteDatabase.OPEN_READONLY | SQLiteDatabase.NO_LOCALIZED_COLLATORS | SQLiteDatabase.CREATE_IF_NECESSARY);
            }
            if (CreatorHelper.createTables(dbConfig, db)) {
                return openReadOnly(dbConfig, db);
            }
            UpgradeHelper.verifyUpgrade(dbConfig, db);
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
