package com.erc.dal;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by einar on 9/6/2015.
 */
public class DBManager extends SQLiteOpenHelper {

    private Context context;
    private Upgradeable upgradeable;
    private static DBManager dbManager;


    private DBManager(Context context) {
        super(context, "test.db", null, 1);
        this.context = context;
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

    public static DBManager getInstance(Context context) {
        if (dbManager == null) {
            dbManager = new DBManager(context);
        }
        return dbManager;
    }

    public void setOnUpgradeListener(Upgradeable upgradeable) {
        this.upgradeable = upgradeable;
    }
}
