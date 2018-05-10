package com.erc.dal;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by einar on 9/6/2015.
 */
public class SQLiteDatabaseManager extends SQLiteOpenHelper {

    private static Map<String, Upgradeable> upgradeableMap = new HashMap<>();
    private static SQLiteDatabaseManager sqLiteDatabaseManager;
    private Context context;
    private DBConfig dbConfig;

    private SQLiteDatabaseManager(DBConfig dbConfigs) {
        super(dbConfigs.getContext(), dbConfigs.getDataBaseName(), null, dbConfigs.getVersion());
        this.context = dbConfigs.getContext();
        this.dbConfig = dbConfigs;
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
        startUpgrade(db, oldVersion, newVersion, dbConfig);
    }

    public static void startUpgrade(SQLiteDatabase db, int oldVersion, int newVersion, DBConfig dbConfig) {
        if (upgradeableMap != null) {
            if (upgradeableMap.containsKey(getFullDatabaseName(dbConfig))) {
                upgradeableMap.get(getFullDatabaseName(dbConfig)).onUpgrade(db, oldVersion, newVersion);
            }
        }
    }

    private static SQLiteDatabaseManager getInstance(DBConfig dbConfig) {
        if (sqLiteDatabaseManager == null) {
            sqLiteDatabaseManager = new SQLiteDatabaseManager(dbConfig);
        }
        return sqLiteDatabaseManager;
    }

    public static SQLiteDatabase open(DBConfig dbConfig) {
        SQLiteDatabase db = null;
        try {
            if (Util.isNullOrEmpty(dbConfig.getUrl())) {
                db = getInstance(dbConfig).getWritableDatabase();
                db.setLockingEnabled(false);
            } else {
                db = SQLiteDatabase.openDatabase(getFullDatabaseName(dbConfig), null, SQLiteDatabase.OPEN_READWRITE | SQLiteDatabase.NO_LOCALIZED_COLLATORS | SQLiteDatabase.CREATE_IF_NECESSARY);
                if (dbConfig.getVersion() != dbConfig.getOldVersion()) {
                    startUpgrade(db, dbConfig.getOldVersion(), dbConfig.getVersion(), dbConfig);
                    dbConfig.setOldVersion(dbConfig.getVersion());
                }
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
                db = SQLiteDatabase.openDatabase(getFullDatabaseName(dbConfig), null, SQLiteDatabase.OPEN_READONLY | SQLiteDatabase.NO_LOCALIZED_COLLATORS | SQLiteDatabase.CREATE_IF_NECESSARY);
                if (dbConfig.getVersion() != dbConfig.getOldVersion()) {
                    startUpgrade(db, dbConfig.getOldVersion(), dbConfig.getVersion(), dbConfig);
                    dbConfig.setOldVersion(dbConfig.getVersion());
                }
            }
        } catch (Exception e) {
            Log.e("Opening database", e);
        }
        return db;
    }

    public static void closeDb() {
        try {
            if (sqLiteDatabaseManager != null) {
                sqLiteDatabaseManager.close();
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

    public static void setOnUpgradeListener(Upgradeable upgradeable, DBConfig dbConfig) {
        if (!upgradeableMap.containsKey(getFullDatabaseName(dbConfig))) {
            upgradeableMap.put(getFullDatabaseName(dbConfig), upgradeable);
        }
    }

    private static String getFullDatabaseName(DBConfig dbConfig) {
        return dbConfig.getUrl() + "/" + dbConfig.getDataBaseName();
    }
}
