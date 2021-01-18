package com.erc.dal.upgrade;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by einar on 10/15/2015.
 */
public interface UpgradeListener {
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion);
}
