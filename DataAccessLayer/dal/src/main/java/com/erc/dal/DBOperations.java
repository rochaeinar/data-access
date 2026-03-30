package com.erc.dal;

import android.content.ContentValues;
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
        Pair pair = QueryBuilder.getPrimaryKey(entity);

        if (pair == null && options.length == 0) {
            throw new DALException("Cannot save " + entity.getClass().getName() + ": no @PrimaryKey defined and no Options provided");
        }

        Entity entityToUpdate = null;
        if (options.length == 0) {
            entityToUpdate = getById(entity.getClass(), Long.parseLong(pair.getValue()), dbConfig);
        } else {
            Options options_ = options[0];
            ArrayList<Object> entitiesToUpdate = getAll(entity.getClass(), dbConfig, options_);
            if (entitiesToUpdate.size() > 0) {
                entityToUpdate = (Entity) entitiesToUpdate.get(0);
            }
        }

        String tableName = QueryBuilder.geTableName(entity.getClass());

        if (entityToUpdate == null) {
            if (pair != null && (pair.getValue().toString().isEmpty() || pair.getValue().toString().equals("0"))) {
                QueryBuilder.setID(entity, this, dbConfig);
            }
        }

        ContentValues cv = buildContentValues(entity);
        db = sqLiteDatabaseManager.open(dbConfig, db);
        try {
            if (entityToUpdate == null) {
                db.insertOrThrow(tableName, null, cv);
            } else {
                if (options.length == 0) {
                    db.update(tableName, cv, pair.getName() + " = ?", new String[]{pair.getValue()});
                } else {
                    // Options-based update: fall back to raw SQL for complex WHERE expressions
                    String sql = QueryBuilder.getQueryUpdate(entity, options);
                    db.execSQL(sql);
                }
            }
        } catch (Exception e) {
            throw new DALException("Failed to save " + entity.getClass().getName() + " to " + tableName, e);
        } finally {
            closeDb(db);
        }
        return (T) entity;
    }

    private ContentValues buildContentValues(Entity entity) {
        ContentValues cv = new ContentValues();
        try {
            java.lang.reflect.Field[] allFields = entity.getClass().getDeclaredFields();
            for (java.lang.reflect.Field field : allFields) {
                if (field.isAnnotationPresent(com.erc.dal.Field.class)) {
                    field.setAccessible(true);
                    String columnName = ReflectionHelper.getDataBaseNameOfField(field);
                    Object value = field.get(entity);
                    if (value == null) {
                        cv.putNull(columnName);
                        continue;
                    }
                    Class<?> type = field.getType();
                    if (type.equals(String.class)) {
                        cv.put(columnName, (String) value);
                    } else if (type.equals(int.class) || type.equals(Integer.class)) {
                        cv.put(columnName, (Integer) value);
                    } else if (type.equals(long.class) || type.equals(Long.class)) {
                        cv.put(columnName, (Long) value);
                    } else if (type.equals(short.class) || type.equals(Short.class)) {
                        cv.put(columnName, (Short) value);
                    } else if (type.equals(boolean.class) || type.equals(Boolean.class)) {
                        cv.put(columnName, ((Boolean) value) ? 1 : 0);
                    } else if (type.equals(double.class) || type.equals(Double.class)) {
                        cv.put(columnName, (Double) value);
                    } else if (type.equals(float.class) || type.equals(Float.class)) {
                        cv.put(columnName, (Float) value);
                    } else if (type.equals(Date.class)) {
                        cv.put(columnName, HelperDate.getDateWithFormat((Date) value, HelperDate.ISO_FORMAT));
                    } else if (type.equals(char.class) || type.equals(Character.class)) {
                        char c = (Character) value;
                        cv.put(columnName, c == '\0' ? "" : String.valueOf(c));
                    } else if (type.equals(byte[].class)) {
                        cv.put(columnName, (byte[]) value);
                    } else {
                        cv.put(columnName, value.toString());
                    }
                }
            }
        } catch (DALException e) {
            throw e;
        } catch (Exception e) {
            throw new DALException("Failed to build ContentValues for " + entity.getClass().getName(), e);
        }
        return cv;
    }

    public synchronized <T> T getById(Class classType, Object id, DBConfig dbConfig) {
        T entity = null;
        String sql = QueryBuilder.getQuery(classType, id);
        Cursor cursor = rawQuery(sql, dbConfig);
        try {
            if (cursor != null && cursor.moveToNext()) {
                entity = (T) ReflectionHelper.getInstance(classType, new Object[]{}, new Class[]{});
                ArrayList<java.lang.reflect.Field> fields = ReflectionHelper.getFields(entity);
                fillFields(fields, cursor, entity);
            }
        } catch (DALException e) {
            throw e;
        } catch (Exception e) {
            throw new DALException("Failed to map result for " + classType.getName(), e);
        } finally {
            closeCursor(cursor);
            closeDb(db);
        }
        return entity;
    }

    public synchronized <T> ArrayList<T> getAll(Class classType, DBConfig dbConfig, Options... options) {
        ArrayList<T> entities = new ArrayList<>();
        Options options_ = options.length == 0 ? new Options() : options[0];
        String selectAll = QueryBuilder.getAllQuery(classType);
        selectAll = options_.getSql(classType, selectAll) + Constant.SEMICOLON;
        Cursor cursor = rawQuery(selectAll, dbConfig);
        try {
            while (cursor != null && cursor.moveToNext()) {
                Object entity = ReflectionHelper.getInstance(classType, new Object[]{}, new Class[]{});
                ArrayList<java.lang.reflect.Field> fields = ReflectionHelper.getFields(entity);
                fillFields(fields, cursor, entity);
                entities.add((T) entity);
            }
        } catch (DALException e) {
            throw e;
        } catch (Exception e) {
            throw new DALException("Failed to map result for " + classType.getName(), e);
        } finally {
            closeCursor(cursor);
            closeDb(db);
        }
        return entities;
    }

    public synchronized <T> T calculate(Class classType, Aggregation aggregationOperator, DBConfig dbConfig, Options... options) {
        if (aggregationOperator == null) {
            throw new DALException("Aggregation operator cannot be null");
        }
        T res = null;
        Options options_ = options.length == 0 ? new Options() : options[0];
        String selectAll = QueryBuilder.getAllQuery(classType);
        selectAll = options_.getSql(classType, selectAll, aggregationOperator) + Constant.SEMICOLON;
        Cursor cursor = rawQuery(selectAll, dbConfig);
        try {
            if (cursor != null && cursor.moveToNext()) {
                if (cursor.getType(0) == Cursor.FIELD_TYPE_FLOAT) {
                    res = (T) new Float(cursor.getFloat(0));
                }
                if (cursor.getType(0) == Cursor.FIELD_TYPE_INTEGER) {
                    res = (T) new Long(cursor.getLong(0));
                }
            }
        } finally {
            closeCursor(cursor);
            closeDb(db);
        }
        return res;
    }

    public synchronized boolean remove(Class classType, Object id, DBConfig dbConfig) {
        String sql = QueryBuilder.getQueryRemove(classType, id);
        boolean success = execSQL(sql, dbConfig);
        closeDb(db);
        return success;
    }

    public Cursor rawQuery(String sql, DBConfig dbConfig) {
        db = sqLiteDatabaseManager.openReadOnly(dbConfig, db);
        try {
            return db.rawQuery(sql, null);
        } catch (Exception e) {
            closeDb(db);
            throw new DALException("Failed to execute raw SQL: " + sql, e);
        }
    }

    public boolean execSQL(String sql, DBConfig dbConfig) {
        db = sqLiteDatabaseManager.open(dbConfig, db);
        try {
            db.execSQL(sql);
            return true;
        } catch (Exception e) {
            closeDb(db);
            throw new DALException("Failed to execute SQL: " + sql, e);
        }
    }

    private void fillFields(ArrayList<java.lang.reflect.Field> fields, Cursor cursor, Object entity) throws Exception {
        Type type;
        for (java.lang.reflect.Field field : fields) {
            Object value = null;
            type = field.getType();
            String currentField = field.getName();

            try {
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

                field.setAccessible(true);
                field.set(entity, value);
            } catch (Exception e) {
                throw new DALException("Failed to fill field \"" + currentField + "\" in " + entity.getClass().getName(), e);
            }
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
        }
    }
}
