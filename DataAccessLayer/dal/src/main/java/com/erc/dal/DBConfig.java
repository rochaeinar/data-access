package com.erc.dal;

import android.content.Context;

/**
 * Created by einar on 10/18/2015.
 */
public class DBConfig {
    private String dataBaseName;
    private int version;
    private String url;
    private Context context;

    public DBConfig(Context context, String dataBaseName, int version, String url) {
        this.context = context;
        this.dataBaseName = dataBaseName;
        this.version = version;
        setUrl(url);
        if (Util.isNullOrEmpty(dataBaseName)) {
            this.dataBaseName = SQLiteDatabaseManager.getDataBaseName(context);
        }
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

    public int getOldVersion() {
        return version;
    }

    public void setOldVersion(int oldVersion) {
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


}
