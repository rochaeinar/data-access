package com.erc.dal.upgrade;

import android.content.Context;

import com.erc.dal.SQLiteDatabaseManager;
import com.erc.dal.Util;

/**
 * Created by einar on 10/18/2015.
 */
public class DBConfig {
    private String dataBaseName;
    private int version;
    private int currentVersionCache;
    private long tableCountCache;
    private String url;
    private Context context;
    private String packageFilter;
    private UpgradeListener upgradeListener;

    public DBConfig(Context context, String dataBaseName, int version, String url) {
        this.context = context;
        this.dataBaseName = dataBaseName;
        this.version = version;
        setUrl(url);
        if (Util.isNullOrEmpty(dataBaseName)) {
            this.dataBaseName = SQLiteDatabaseManager.getDataBaseName(context);
        }
    }

    public UpgradeListener getUpgradeListener() {
        return upgradeListener;
    }

    public void setOnUpgradeListener(UpgradeListener upgradeListener) {
        this.upgradeListener = upgradeListener;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public String getDataBaseName() {
        return dataBaseName;
    }

    public void setDataBaseName(String dataBaseName) {
        this.dataBaseName = dataBaseName;
    }

    public int getVersion() {
        return version;
    }

    int getCurrentVersionCache() {
        return currentVersionCache;
    }

    void setCurrentVersionCache(int currentVersionCache) {
        this.currentVersionCache = currentVersionCache;
    }

    public long getTableCountCache() {
        return tableCountCache;
    }

    public void setTableCountCache(long tableCountCache) {
        this.tableCountCache = tableCountCache;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        if (url == null) {
            url = "";
        }
        this.url = url.replaceAll("/$", "");
    }

    public String getPackageFilter() {
        return packageFilter;
    }

    public void setPackageFilter(String packageFilter) {
        this.packageFilter = packageFilter;
    }

    public void clearCache() {
        currentVersionCache = 0;
        tableCountCache = 0;
    }

    public String getFullDatabaseName() {
        return getUrl() + "/" + getDataBaseName();
    }
}
