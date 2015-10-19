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
        this.url = url;
        if (Util.isNullOrEmpty(dataBaseName)) {
            this.dataBaseName = DBManager.getDataBaseName(context);
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

    public void setVersion(int version) {
        this.version = version;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }


}
