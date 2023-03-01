package com.erc.dataaccesslayer;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.erc.dal.Log;
import com.erc.dal.upgrade.UpgradeListener;

public class UpgradeExample implements UpgradeListener {
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 0) {
            try {
                db.execSQL("ALTER TABLE SETTINGS ADD COLUMN UPGRADE_EXAMPLE REAL DEFAULT 0.0;");
            } catch (Exception e) {
                Log.w("UpgradeExample.onUpgrade");
            }
        } else if (newVersion != 1) {
            db.execSQL("ALTER TABLE SETTINGS ADD COLUMN RANDOM_" + newVersion + " REAL DEFAULT 0.0;");

            if (!fieldExist(db, "SETTINGS", "UPGRADED_VALUE")) {
                db.execSQL("ALTER TABLE SETTINGS ADD COLUMN UPGRADED_VALUE REAL DEFAULT 0.0;");
            }
        }
    }

    public boolean fieldExist(SQLiteDatabase db, String tableName, String fieldName) {
        boolean isExist = false;

        Cursor res = null;

        try {
            res = db.rawQuery("Select * from " + tableName + " limit 1", null);

            int colIndex = res.getColumnIndex(fieldName);
            if (colIndex != -1) {
                isExist = true;
            }
        } catch (Exception e) {
        } finally {
            try {
                if (res != null) {
                    res.close();
                }
            } catch (Exception e1) {
            }
        }
        return isExist;
    }
}
