package com.erc.dal;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;

import java.io.File;

/**
 * Created by einar on 9/6/2015.
 */
public class DBManager extends SQLiteOpenHelper {

    private static Upgradeable upgradeable;
    private static DBManager dbManager;
    private Context context;

    private DBManager(DBConfig dbConfigs) {
        super(dbConfigs.getContext(), dbConfigs.getDataBaseName(), null, dbConfigs.getVersion());
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.w("Database created");
        String sql = QueryBuilder.getCreateQuery(context, Table.class);
        for (String sqlCreate : sql.split(";")) {
            db.execSQL(sqlCreate);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        upgradeable.onUpgrade(db, oldVersion, newVersion);
    }

    private static DBManager getInstance(DBConfig dbConfig) {
        if (dbManager == null) {
            dbManager = new DBManager(dbConfig);
        }
        return dbManager;
    }

    public static SQLiteDatabase open(DBConfig dbConfig) {
        SQLiteDatabase db = null;
        try {
            if (Util.isNullOrEmpty(dbConfig.getUrl())) {
                db = getInstance(dbConfig).getWritableDatabase();
                db.setLockingEnabled(false);
            } else {
                File path = Environment.getExternalStorageDirectory();
                db = SQLiteDatabase.openDatabase(path.getAbsolutePath() + dbConfig.getUrl(), null, SQLiteDatabase.OPEN_READWRITE);
            }
        } catch (Exception e) {
            Log.e("Opening database", e);
        }
        return db;
    }

    public static SQLiteDatabase openReadOnly(DBConfig dbConfig) {
        SQLiteDatabase db = null;
        try {
            if (Util.isNullOrEmpty(dbConfig.getUrl())) {
                db = getInstance(dbConfig).getWritableDatabase();
                db.setLockingEnabled(false);
            } else {
                File path = Environment.getExternalStorageDirectory();
                db = SQLiteDatabase.openDatabase(path.getAbsolutePath() + dbConfig.getUrl(), null, SQLiteDatabase.OPEN_READONLY);
            }
        } catch (Exception e) {
            Log.e("Opening database", e);
        }
        return db;
    }

    public static void closeDb() {
        try {
            if (dbManager != null) {
                dbManager.close();
            }
        } catch (Exception e) {
            Log.e("Closing data base", e);
        }
    }

    public static String getDataBaseName(Context context) {
        String dataBaseName = RegEx.match(context.getPackageName(), RegEx.PATTERN_EXTENSION);
        dataBaseName = dataBaseName.replace(".", "");
        return dataBaseName;
    }

    public static void setOnUpgradeListener(Upgradeable upgradeable) {
        DBManager.upgradeable = upgradeable;
    }
}
