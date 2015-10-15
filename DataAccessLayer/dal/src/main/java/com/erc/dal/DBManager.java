package com.erc.dal;

import android.content.Context;

/**
 * Created by einar on 9/6/2015.
 */
public class DBManager {

    private Context context;
    private String dataBaseName;

    public DBManager(Context context, String dataBaseName) {
        this.context = context;
        this.dataBaseName = dataBaseName;
    }

    public void createDataBasse() {
        String sql = QueryBuilder.getCreateQuery(context, Table.class);
        Log.i(sql);
    }

}
