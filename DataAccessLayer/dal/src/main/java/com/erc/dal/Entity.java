package com.erc.dal;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by einar on 8/31/2015.
 */
public abstract class Entity<T> {

    private DBConfig dbConfig;

    public Entity(Context context) {
        dbConfig = new DBConfig(context, null, 1, null);
    }

    public Entity(DBConfig dbConfig) {
        this.dbConfig = dbConfig;
    }

    public Entity save() {
        String sql = "";
        Pair pair = QueryBuilder.getPrimaryKey(this);
        if (pair.getValue().toString().isEmpty() || pair.getValue().toString().equals("0")) {
            QueryBuilder.setID(this, dbConfig);
            sql = QueryBuilder.getQueryInsert(this);
        } else {
            sql = QueryBuilder.getQueryUpdate(this);
        }
        execSQL(sql);
        return this;
    }

    public ArrayList<T> getAll(Options... options) {
        ArrayList<T> entities = new ArrayList<>();
        Options options_ = options.length == 0 ? new Options() : options[0];
        String selectAll = QueryBuilder.getAllQuery(this.getClass());
        selectAll = options_.getSql(this.getClass(), selectAll) + Ctt.SEMICOLON;
        Cursor cursor = rawQuery(selectAll);
        while (cursor.moveToNext()) {
            try {
                Object entity = ReflectionHelper.getInstance(this.getClass(), new Object[]{dbConfig.getContext()}, new Class[]{Context.class});
                ArrayList<Field> fields = ReflectionHelper.getFields(entity);
                fillFields(fields, cursor, entity);
                entities.add((T) entity);
            } catch (Exception e) {
                Log.e("Fail to fill getAll", e);
            }
        }
        return entities;
    }

    public <T> T getById(long id) {
        T entity = null;
        String sql = QueryBuilder.getQuery(this.getClass(), id);
        Cursor cursor = rawQuery(sql);
        if (cursor.moveToNext()) {
            try {
                entity = (T) ReflectionHelper.getInstance(this.getClass(), new Object[]{dbConfig.getContext()}, new Class[]{Context.class});
                ArrayList<Field> fields = ReflectionHelper.getFields(entity);
                fillFields(fields, cursor, entity);
            } catch (Exception e) {
                Log.e("Fail to fill getById", e);
            }
        }
        return entity;
    }

    public boolean remove(int id) {
        String sql = QueryBuilder.getQueryRemove(this.getClass(), id);
        return execSQL(sql);
    }

    public boolean execSQL(String sql) {
        SQLiteDatabase db = DBManager.open(dbConfig);
        boolean res = false;
        try {
            db.execSQL(sql);
            //Log.i(sql);
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
        SQLiteDatabase db = DBManager.openReadOnly(dbConfig);
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(sql, null);
            //Log.i(sql);
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

    public long calculate(Aggregation aggregationOperator, Options... options) {
        long res = 0;
        try {
            if (aggregationOperator != null) {
                Options options_ = options.length == 0 ? new Options() : options[0];
                String selectAll = QueryBuilder.getAllQuery(this.getClass());
                selectAll = options_.getSql(this.getClass(), selectAll, aggregationOperator) + Ctt.SEMICOLON;
                Cursor cursor = rawQuery(selectAll);
                if (cursor.moveToNext()) {
                    res = cursor.getLong(0);
                }
            } else {
                Log.w("null aggregation Operator on Entity.Calculate");
            }
        } catch (Exception e) {
            Log.e("fail to calculate:" + aggregationOperator.getOperator(), e);
        }
        return res;
    }

    private void fillFields(ArrayList<Field> fields, Cursor cursor, Object entity) throws IllegalAccessException {
        for (Field field : fields) {
            Object value = null;
            Type type = field.getType();
            do {
                if (type.equals(String.class)) {
                    value = cursor.getString(cursor.getColumnIndex(ReflectionHelper.getDataBaseNameOfField(field)));
                    break;
                }
                if (type.equals(char.class) || type.equals(Character.class)) {
                    String charText = cursor.getString(cursor.getColumnIndex(ReflectionHelper.getDataBaseNameOfField(field)));
                    value = Util.isNullOrEmpty(charText) ? '\0' : charText.toCharArray()[0];
                    break;
                }
                if (type.equals(Date.class)) {
                    String dateIso = cursor.getString(cursor.getColumnIndex(ReflectionHelper.getDataBaseNameOfField(field)));
                    value = HelperDate.getDateFromFormat(dateIso, HelperDate.ISO_FORMAT);
                    break;
                }
                if (type.equals(short.class) || type.equals(Short.class)) {
                    value = cursor.getShort(cursor.getColumnIndex(ReflectionHelper.getDataBaseNameOfField(field)));
                    break;
                }
                if (type.equals(int.class) || type.equals(Integer.class)) {
                    value = cursor.getInt(cursor.getColumnIndex(ReflectionHelper.getDataBaseNameOfField(field)));
                    break;
                }
                if (type.equals(long.class) || type.equals(Long.class)) {
                    value = cursor.getLong(cursor.getColumnIndex(ReflectionHelper.getDataBaseNameOfField(field)));
                    break;
                }
                if (type.equals(boolean.class) || type.equals(Boolean.class)) {
                    value = cursor.getInt(cursor.getColumnIndex(ReflectionHelper.getDataBaseNameOfField(field))) == 1;
                    break;
                }
                if (type.equals(double.class) || type.equals(Double.class)) {
                    value = cursor.getDouble(cursor.getColumnIndex(ReflectionHelper.getDataBaseNameOfField(field)));
                    break;
                }
                if (type.equals(float.class) || type.equals(Float.class)) {
                    value = cursor.getFloat(cursor.getColumnIndex(ReflectionHelper.getDataBaseNameOfField(field)));
                    break;
                }
                break;
            } while (true);

            field.set(entity, value);
        }
    }
}
