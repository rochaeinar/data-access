package com.erc.dal;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.renderscript.Sampler;

import java.lang.reflect.Constructor;
import java.util.ArrayList;

/**
 * Created by einar on 8/31/2015.
 */
public abstract class Entity {

    private Context context;

    public Entity(Context context) {
        this.context = context;
    }

    public Entity save() {
        String sql = "";
        Pair pair = QueryBuilder.getPrimaryKey(this);
        if (pair.getValue().toString().isEmpty() || pair.getValue().toString().equals("0")) {
            QueryBuilder.setID(this);
            sql = QueryBuilder.getQueryInsert(this);
        } else {
            sql = QueryBuilder.getQueryUpdate(this);
        }
        execSQL(sql);
        return this;
    }

    public ArrayList<Entity> getAll(Options... options) {
        Options options_ = options.length == 0 ? new Options() : options[0];
        String selectAll = QueryBuilder.getAllQuery(this.getClass());
        selectAll = options_.getSql(this, selectAll) + Ctt.SEMICOLON;
        rawQuery(selectAll);
        return new ArrayList<Entity>();
    }

    public Entity get(long id) {
        String sql = QueryBuilder.getQuery(this.getClass(), id);
        Cursor cursor = rawQuery(sql);
        while (cursor.moveToNext()) {
            try {
                Class<?> clazz = Class.forName(this.getClass().getName());
                Constructor<?> ctor = clazz.getConstructor(Context.class);
                Object object = ctor.newInstance(new Object[]{context});
            } catch (Exception e) {
                Log.e("Fail to fiill data", e);
            }
            //sms_received.setSMS_ID(c.getString(c.getColumnIndex("SMS_ID")));
        }
        return this;
    }

    public boolean remove(int id) {
        String sql = QueryBuilder.getQueryRemove(this.getClass(), id);
        return execSQL(sql);
    }

    public boolean execSQL(String sql) {
        DBManager dbManager = DBManager.getInstance(context);
        SQLiteDatabase db = dbManager.getReadableDatabase();
        boolean res = false;
        try {
            db.execSQL(sql);
            Log.i(sql);
            res = true;
        } catch (Exception e) {
            Log.e("Failed to execute SQL", e);
        }/*finally {
            try {
                db.close();
            } catch (Exception ex) {
                Log.e("Fail to close db", ex);
            }
        }*/
        return res;
    }

    public Cursor rawQuery(String sql) {
        DBManager dbManager = DBManager.getInstance(context);
        SQLiteDatabase db = dbManager.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(sql, null);
            Log.i(sql);
        } catch (Exception e) {
            Log.e("Failed to execute SQL", e);
        } /*finally {
            try {
                db.close();
                cursor.close();
            } catch (Exception ex) {
                Log.e("Fail to close raw db", ex);
            }
        }*/
        return cursor;
    }
}
