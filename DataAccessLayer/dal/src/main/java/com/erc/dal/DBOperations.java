package com.erc.dal;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by einar on 10/17/2016.
 */
class DBOperations {
    SQLiteDatabase db;
    private static DBOperations dbOperations;

    private DBOperations() {
    }

    public static DBOperations getInstance() {
        if (dbOperations == null) {
            dbOperations = new DBOperations();
        }
        return dbOperations;
    }

    public synchronized Entity save(Entity entity, DBConfig dbConfig) {
        String sql = "";
        Pair pair = QueryBuilder.getPrimaryKey(entity);
        if (pair != null) {
            Entity entityToUpdate = getById(entity.getClass(), Long.parseLong(pair.getValue()), dbConfig);
            if (entityToUpdate == null) {
                if (pair.getValue().toString().isEmpty() || pair.getValue().toString().equals("0")) {
                    QueryBuilder.setID(entity, this, dbConfig);
                }
                sql = QueryBuilder.getQueryInsert(entity);
            } else {
                sql = QueryBuilder.getQueryUpdate(entity);
            }
            execSQL(sql, dbConfig);
            db.close();
            return entity;
        } else {
            return null;
        }
    }

    public synchronized <T> T getById(Class classType, Object id, DBConfig dbConfig) {
        T entity = null;
        String sql = QueryBuilder.getQuery(classType, id);
        if (!Util.isNullOrEmpty(sql)) {
            Cursor cursor = rawQuery(sql, dbConfig);
            if (cursor != null && cursor.moveToNext()) {
                try {
                    entity = (T) ReflectionHelper.getInstance(classType, new Object[]{}, new Class[]{});
                    ArrayList<java.lang.reflect.Field> fields = ReflectionHelper.getFields(entity);
                    fillFields(fields, cursor, entity);
                } catch (Exception e) {
                    Log.e("Fail to fill getById", e);
                }
            }
            db.close();
            return entity;
        } else {
            return null;
        }
    }

    public synchronized <T> ArrayList<T> getAll(Class classType, DBConfig dbConfig, Options... options) {
        ArrayList<T> entities = new ArrayList<>();
        Options options_ = options.length == 0 ? new Options() : options[0];
        String selectAll = QueryBuilder.getAllQuery(classType);
        selectAll = options_.getSql(classType, selectAll) + Constant.SEMICOLON;
        Cursor cursor = rawQuery(selectAll, dbConfig);
        while (cursor != null && cursor.moveToNext()) {
            try {
                Object entity = ReflectionHelper.getInstance(classType, new Object[]{}, new Class[]{});
                ArrayList<java.lang.reflect.Field> fields = ReflectionHelper.getFields(entity);
                fillFields(fields, cursor, entity);
                entities.add((T) entity);
            } catch (Exception e) {
                Log.e("Fail to fill getAll", e);
            }
        }
        db.close();
        return entities;
    }

    public synchronized long calculate(Class classType, Aggregation aggregationOperator, DBConfig dbConfig, Options... options) {
        long res = 0;
        try {
            if (aggregationOperator != null) {
                Options options_ = options.length == 0 ? new Options() : options[0];
                String selectAll = QueryBuilder.getAllQuery(classType);
                selectAll = options_.getSql(classType, selectAll, aggregationOperator) + Constant.SEMICOLON;
                Cursor cursor = rawQuery(selectAll, dbConfig);
                if (cursor != null && cursor.moveToNext()) {
                    res = cursor.getLong(0);
                }
                db.close();
            } else {
                Log.w("null aggregation Operator on Entity.Calculate");
            }
        } catch (Exception e) {
            Log.e("fail to calculate:" + aggregationOperator.getOperator(), e);
        }
        return res;
    }

    public synchronized boolean remove(Class classType, Object id, DBConfig dbConfig) {
        String sql = QueryBuilder.getQueryRemove(classType, id);
        if (!Util.isNullOrEmpty(sql)) {
            boolean success = execSQL(sql, dbConfig);
            db.close();
            return success;
        }
        return false;
    }

    private Cursor rawQuery(String sql, DBConfig dbConfig) {
        db = SQLiteDatabaseManager.openReadOnly(dbConfig);
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(sql, null);
        } catch (Exception e) {
            Log.e("Failed to execute raw SQL", e);
        } finally {
        }
        return cursor;
    }

    public boolean execSQL(String sql, DBConfig dbConfig) {
        db = SQLiteDatabaseManager.open(dbConfig);
        boolean res = false;
        try {
            db.execSQL(sql);
            //Log.i(sql);
            res = true;
        } catch (Exception e) {
            Log.e("Failed to execute SQL", e);
        } finally {
        }
        return res;
    }

    private static void fillFields(ArrayList<java.lang.reflect.Field> fields, Cursor cursor, Object entity) throws IllegalAccessException {
        Type type;
        String currentField = "null";
        try {
            for (java.lang.reflect.Field field : fields) {
                Object value = null;
                type = field.getType();
                currentField = field.getName();

                do {
                    if (ReflectionHelper.getDataBaseNameOfField(field).equals("rowid")) {
                        value = -1;
                        break;
                    }
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
        } catch (Exception e) {
            Log.e("Failed to fill Field: \"" + currentField + "\" in " + entity.getClass().getName(), e);
        }
    }

}
