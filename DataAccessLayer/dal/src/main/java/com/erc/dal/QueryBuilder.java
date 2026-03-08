package com.erc.dal;

import android.text.TextUtils;

import com.erc.dal.upgrade.DBConfig;
import com.erc.dal.util.StringUtil;

import java.lang.reflect.Field;
import java.util.ArrayList;

/**
 * Created by einar on 8/6/2015.
 */
public class QueryBuilder {

    public static void setID(Entity entity, DBOperations dbOperations, DBConfig dbConfig) {
        try {
            Field[] fields = entity.getClass().getDeclaredFields();
            for (Field field : fields) {
                if (field.isAnnotationPresent(PrimaryKey.class)) {
                    field.setAccessible(true);
                    long id = generateId(entity, dbOperations, dbConfig);
                    if (field.getType().equals(String.class)) {
                        field.set(entity, Util.fillRight(Long.toString(id), Constant.LENGTH_TEXT_ID, '0'));
                    } else {
                        field.set(entity, id);
                    }
                    break;
                }
            }
        } catch (DALException e) {
            throw e;
        } catch (Exception e) {
            throw new DALException("Failed to set ID on " + entity.getClass().getName(), e);
        }
    }

    private static long generateId(Entity entity, DBOperations db, DBConfig dbConfig) {
        String nameId = getPrimaryKey(entity).getName();
        Object calculatedValue = db.calculate(entity.getClass(), Aggregation.max(nameId), dbConfig);
        return (calculatedValue == null ? 0 : (long) calculatedValue) + 1;
    }

    public static Pair getPrimaryKey(Entity entity) {
        try {
            Field[] fields = entity.getClass().getDeclaredFields();
            for (Field field : fields) {
                if (field.isAnnotationPresent(PrimaryKey.class)) {
                    field.setAccessible(true);
                    Pair pair = new Pair();
                    pair.setName(ReflectionHelper.getDataBaseNameOfField(field));
                    pair.setType(field.getType());
                    pair.setValue(Util.getValueFromField(field, entity));
                    return pair;
                }
            }
        } catch (Exception e) {
            throw new DALException("Failed to get primary key from " + entity.getClass().getName(), e);
        }
        return null;
    }

    public static Pair getPrimaryKey(Class classType, Object id) {
        try {
            Field[] fields = classType.getDeclaredFields();
            for (Field field : fields) {
                if (field.isAnnotationPresent(PrimaryKey.class)) {
                    field.setAccessible(true);
                    Pair pair = new Pair();
                    pair.setName(ReflectionHelper.getDataBaseNameOfField(field));
                    pair.setType(field.getType());
                    pair.setValue(id.toString());
                    return pair;
                }
            }
        } catch (Exception e) {
            throw new DALException("Failed to get primary key from " + classType.getName(), e);
        }
        throw new DALException("No @PrimaryKey annotation found on " + classType.getName());
    }

    private static NameValue getNamesValues(Entity entity) {
        if (entity == null) {
            throw new DALException("Cannot build INSERT values: entity is null");
        }
        ArrayList<String> fields = new ArrayList<String>();
        ArrayList<String> values = new ArrayList<String>();
        try {
            Field[] allFields = entity.getClass().getDeclaredFields();
            for (Field field : allFields) {
                if (field.isAnnotationPresent(com.erc.dal.Field.class)) {
                    field.setAccessible(true);
                    fields.add(ReflectionHelper.getDataBaseNameOfField(field));
                    if (HelperDataType.hasCuotes(field.getType())) {
                        String raw = Util.getValueFromField(field, entity);
                        String escaped = raw != null ? raw.replace("'", "''") : "";
                        String namedValue = StringUtil.replaceLiteral(Constant.VALUE_QUOTES, Constant.VALUE, escaped);
                        values.add(namedValue);
                    } else {
                        values.add(Util.getValueFromField(field, entity));
                    }
                }
            }
        } catch (DALException e) {
            throw e;
        } catch (Exception e) {
            throw new DALException("Failed to build INSERT values for " + entity.getClass().getName(), e);
        }
        return new NameValue(TextUtils.join(",", fields), TextUtils.join(",", values));
    }

    private static String getPairs(Entity entity) {
        ArrayList<String> pairs = new ArrayList<String>();
        try {
            Field[] allFields = entity.getClass().getDeclaredFields();
            for (Field field : allFields) {
                if (field.isAnnotationPresent(com.erc.dal.Field.class)) {
                    field.setAccessible(true);
                    String name = ReflectionHelper.getDataBaseNameOfField(field);
                    if (HelperDataType.hasCuotes(field.getType())) {
                        String raw = Util.getValueFromField(field, entity);
                        String escaped = raw != null ? raw.replace("'", "''") : "";
                        String namedValue = StringUtil.replaceLiteral(Constant.PAIR_QUOTE, Constant.FIELD, name);
                        namedValue = StringUtil.replaceLiteral(namedValue, Constant.VALUE, escaped);
                        pairs.add(namedValue);
                    } else {
                        String namedValue = StringUtil.replaceLiteral(Constant.PAIR, Constant.FIELD, name);
                        namedValue = StringUtil.replaceLiteral(namedValue, Constant.VALUE, Util.getValueFromField(field, entity));
                        pairs.add(namedValue);
                    }
                }
            }
        } catch (DALException e) {
            throw e;
        } catch (Exception e) {
            throw new DALException("Failed to build UPDATE pairs for " + entity.getClass().getName(), e);
        }
        return TextUtils.join(",", pairs);
    }

    private static String getPairsToCreate(Class entity) {
        ArrayList<String> pairs = new ArrayList<>();
        try {
            Field[] allFields = entity.getDeclaredFields();
            for (Field field : allFields) {
                if (field.isAnnotationPresent(com.erc.dal.Field.class)) {
                    field.setAccessible(true);
                    pairs.add(ReflectionHelper.getDataBaseNameOfField(field) + " " + HelperDataType.getDataBaseType(field.getType()));
                }
            }
        } catch (DALException e) {
            throw e;
        } catch (Exception e) {
            throw new DALException("Failed to build CREATE TABLE columns for " + entity.getName(), e);
        }
        return TextUtils.join(",", pairs);
    }

    public static String geTableName(Class entity) {
        Table table = (Table) entity.getAnnotation(Table.class);
        if (table == null) {
            throw new DALException("Missing @Table annotation on " + entity.getName());
        }
        if (table.name() == null || table.name().isEmpty()) {
            return entity.getSimpleName();
        }
        return table.name();
    }

    public static String getCreateQuery(DBConfig dbConfig, Class type) {
        StringBuilder sb = new StringBuilder();

        String packageName = dbConfig.getContext().getPackageName();

        if (!Util.isNullOrEmpty(dbConfig.getPackageFilter())) {
            packageName = dbConfig.getPackageFilter();
        }

        ArrayList<String> findClassesStartWith = DexFileHelper.findClassesStartWith(packageName);
        for (String className : findClassesStartWith) {
            try {
                Class entity = dbConfig.getContext().getClassLoader().loadClass(className);
                if (entity.isAnnotationPresent(type)) {
                    String namedValue = StringUtil.replaceLiteral(Constant.CREATE, Constant.TABLE, geTableName(entity));
                    namedValue = StringUtil.replaceLiteral(namedValue, Constant.FIELDS, getPairsToCreate(entity));
                    sb.append(namedValue);
                }
            } catch (ClassNotFoundException e) {
                Log.e("Error findSubClasses.ClassNotFoundException", e);
            }
        }

        return sb.toString();
    }

    public static String getAllQuery(Class entity) {
        return StringUtil.replaceLiteral(Constant.SELECT_FROM, "%t", geTableName(entity));
    }

    public static String getQuery(Class classType, Object id) {
        Pair pair = getPrimaryKey(classType, id);
        StringBuffer sb = new StringBuffer();
        sb.append(Constant.SELECT_FROM);
        sb.append(Constant.WHERE);
        sb.append(pair.toString());
        sb.append(Constant.SEMICOLON);
        return StringUtil.replaceLiteral(sb.toString(), Constant.TABLE, geTableName(classType));
    }

    public static String getQueryRemove(Class entity, Object id) {
        Pair pair = getPrimaryKey(entity, id);
        StringBuffer sb = new StringBuffer();
        sb.append(StringUtil.replaceLiteral(Constant.DELETE, Constant.KEYS, pair.toString()));
        return StringUtil.replaceLiteral(sb.toString(), Constant.TABLE, geTableName(entity));
    }

    public static String getQueryInsert(Entity entity) {
        NameValue namesValues = getNamesValues(entity);
        String namedValue = StringUtil.replaceLiteral(Constant.INSERT, Constant.FIELDS, namesValues.getName().toString());
        namedValue = StringUtil.replaceLiteral(namedValue, Constant.VALUES, namesValues.getValue().toString());
        return namedValue.replaceAll(Constant.TABLE, geTableName(entity.getClass()));
    }

    public static String getQueryUpdate(Entity entity, Options... options) {
        String namedValue = StringUtil.replaceLiteral(Constant.UPDATE, Constant.PAIRS, getPairs(entity));
        Pair pair = getPrimaryKey(entity);
        if (pair != null && options.length == 0) {
            namedValue = StringUtil.replaceLiteral(namedValue, Constant.KEYS, pair.toString());
        } else if (options.length > 0) {
            namedValue = StringUtil.replaceLiteral(namedValue, Constant.KEYS, options[0].getExpressions());
        }
        return StringUtil.replaceLiteral(namedValue, Constant.TABLE, geTableName(entity.getClass()));
    }


}
