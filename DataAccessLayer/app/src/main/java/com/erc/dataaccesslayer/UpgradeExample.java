package com.erc.dataaccesslayer;

import android.database.sqlite.SQLiteDatabase;

import com.erc.dal.Log;
import com.erc.dal.upgrade.Upgradeable;

public class UpgradeExample implements Upgradeable {
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 0) {
            try {
                db.execSQL("ALTER TABLE SETTINGS ADD COLUMN UPGRADE_EXAMPLE TEXT;");
            } catch (Exception e) {
                Log.w("UpgradeExample.onUpgrade");
            }
        }
    }
}
