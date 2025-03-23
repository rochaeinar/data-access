package com.erc.dal;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.erc.dal.upgrade.DBConfig;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by einar on 10/17/2016.
 */
class DBOperations {
    SQLiteDatabase db;
    SQLiteDatabaseManager sqLiteDatabaseManager;
    DBConfig dbConfig;

    public DBOperations(DBConfig dbConfig) {
        sqLiteDatabaseManager = new SQLiteDatabaseManager(dbConfig);
        this.dbConfig = dbConfig;
    }

    public synchronized void initialize() {
        dbConfig.clearCache();
        db = sqLiteDatabaseManager.open(dbConfig, db);
        closeDb(db);
    }

    public synchronized <T> T save(Entity entity, DBConfig dbConfig, Options... options) {
        String sql = "";
        Pair pair = QueryBuilder.getPrimaryKey(entity);

        if (pair != null || options.length > 0) {
            Entity entityToUpdate = null;
            if (options.length == 0) {
                entityToUpdate = getById(entity.getClass(), Long.parseLong(pair.getValue()), dbConfig);
            }else {
                Options options_ = options[0];
                ArrayList<Object> entitiesToUpdate = getAll(entity.getClass(), dbConfig, options_);
                if(entitiesToUpdate.size() > 0){
                    entityToUpdate = (Entity)entitiesToUpdate.get(0);
                }
            }
            if (entityToUpdate == null) {
                if (pair.getValue().toString().isEmpty() || pair.getValue().toString().equals("0")) {
                    QueryBuilder.setID(entity, this, dbConfig);
                }
                sql = QueryBuilder.getQueryInsert(entity);
            } else {
                sql = QueryBuilder.getQueryUpdate(entity, options);
            }
            execSQL(sql, dbConfig);
            closeDb(db);
            return (T)entity;
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
            closeCursor(cursor);
            closeDb(db);
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
        closeCursor(cursor);
        closeDb(db);
        return entities;
    }

    public synchronized <T> T calculate(Class classType, Aggregation aggregationOperator, DBConfig dbConfig, Options... options) {
        T res = null;
        try {
            if (aggregationOperator != null) {
                Options options_ = options.length == 0 ? new Options() : options[0];
                String selectAll = QueryBuilder.getAllQuery(classType);
                selectAll = options_.getSql(classType, selectAll, aggregationOperator) + Constant.SEMICOLON;
                Cursor cursor = rawQuery(selectAll, dbConfig);
                if (cursor != null && cursor.moveToNext()) {

                    if (cursor.getType(0) == Cursor.FIELD_TYPE_FLOAT) {
                        res = (T) new Float(cursor.getFloat(0));
                    }

                    if (cursor.getType(0) == Cursor.FIELD_TYPE_INTEGER) {
                        res = (T) new Long(cursor.getLong(0));
                    }
                }
                closeCursor(cursor);
                closeDb(db);
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
            closeDb(db);
            return success;
        }
        return false;
    }

    public Cursor rawQuery(String sql, DBConfig dbConfig) {
        db = sqLiteDatabaseManager.openReadOnly(dbConfig, db);
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
        db = sqLiteDatabaseManager.open(dbConfig, db);
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

    private void fillFields(ArrayList<java.lang.reflect.Field> fields, Cursor cursor, Object entity) throws IllegalAccessException {
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

    private void closeCursor(Cursor cursor) {
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
    }

    private void closeDb(SQLiteDatabase db) {
        if (db != null && db.isOpen()) {
            db.close();
            db.releaseReference();
        }
    }
}
