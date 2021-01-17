package com.erc.dal;

import android.content.Context;

import com.erc.dal.upgrade.Upgradeable;

/**
 * Created by einar on 10/18/2015.
 */
public class DBConfig {
    private String dataBaseName;
    private int version;
    private int currentVersionCache;
    private String url;
    private Context context;
    private String packageFilter;
    private Upgradeable upgradeable;

    public DBConfig(Context context, String dataBaseName, int version, String url) {
        this.context = context;
        this.dataBaseName = dataBaseName;
        this.version = version;
        setUrl(url);
        if (Util.isNullOrEmpty(dataBaseName)) {
            this.dataBaseName = SQLiteDatabaseManager.getDataBaseName(context);
        }
    }

    public Upgradeable getUpgradeable() {
        return upgradeable;
    }

    public void setUpgradeable(Upgradeable upgradeable) {
        this.upgradeable = upgradeable;
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

    public int getCurrentVersionCache() {
        return currentVersionCache;
    }

    public void setCurrentVersionCache(int currentVersionCache) {
        this.currentVersionCache = currentVersionCache;
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
}
