package com.erc.dal;

import android.text.TextUtils;

import com.erc.dal.upgrade.DBConfig;

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
                    long id = generateId(entity, dbOperations, dbConfig);
                    if (field.getType().equals(String.class)) {
                        field.set(entity, Util.fillRight(Long.toString(id), Constant.LENGTH_TEXT_ID, '0'));
                    } else {
                        field.set(entity, id);
                    }
                    break;
                }
            }
        } catch (Exception e) {
            Log.e("Error: setID", e);
        }
    }

    private static long generateId(Entity entity, DBOperations db, DBConfig dbConfig) {
        String nameId = getPrimaryKey(entity).getName();
        Object calculatedValue = db.calculate(entity.getClass(), Aggregation.max(nameId), dbConfig);
        return (calculatedValue == null ? 0 : (long) calculatedValue) + 1;
    }

    public static Pair getPrimaryKey(Entity entity) {
        boolean existPrimaryKey = false;
        Pair pair = null;
        try {
            Field[] fields = entity.getClass().getDeclaredFields();
            for (Field field : fields) {
                if (field.isAnnotationPresent(PrimaryKey.class)) {
                    pair = new Pair();
                    pair.setName(ReflectionHelper.getDataBaseNameOfField(field));
                    pair.setType(field.getType());
                    pair.setValue(Util.getValueFromField(field, entity));
                    existPrimaryKey = true;
                    break;
                }
            }
            if (!existPrimaryKey) {
                Log.e("No Primary Key defined, please add an annotation '@PrimaryKey' on one field inside :" + entity.getClass().getName(), null);
            }
        } catch (Exception e) {
            Log.e("Error: getPrimaryKey", e);
        }
        return pair;
    }

    public static Pair getPrimaryKey(Class classType, Object id) {
        Pair pair = null;
        boolean existPrimaryKey = false;
        try {
            Field[] fields = classType.getDeclaredFields();
            for (Field field : fields) {
                if (field.isAnnotationPresent(PrimaryKey.class)) {
                    pair = new Pair();
                    pair.setName(ReflectionHelper.getDataBaseNameOfField(field));
                    pair.setType(field.getType());
                    pair.setValue(id.toString());
                    existPrimaryKey = true;
                    break;
                }
            }
            if (!existPrimaryKey) {
                Log.e("No Primary Key defined, please add an annotation '@PrimaryKey' on one field inside of " + classType.getName(), null);
            }
        } catch (Exception e) {
            Log.e("Error: getPrimaryKey2", e);
        }
        return pair;
    }

    private static NameValue getNamesValues(Entity entity) {
        ArrayList<String> fields = new ArrayList<String>();
        ArrayList<String> values = new ArrayList<String>();
        try {
            if (entity != null) {
                Field[] allFields = entity.getClass().getDeclaredFields();
                for (Field field : allFields) {
                    if (field.isAnnotationPresent(com.erc.dal.Field.class)) {
                        fields.add(ReflectionHelper.getDataBaseNameOfField(field));
                        if (HelperDataType.hasCuotes(field.getType())) {
                            values.add(Constant.VALUE_QUOTES.replaceFirst(Constant.VALUE, Util.getValueFromField(field, entity)));
                        } else {
                            values.add(Util.getValueFromField(field, entity));
                        }
                    }
                }
            } else {
                Log.e("Error: getNamesValues, entity is null", null);
            }
        } catch (Exception e) {
            Log.e("Error: getNamesValues", e);
        }
        return new NameValue(TextUtils.join(",", fields), TextUtils.join(",", values));
    }

    private static String getPairs(Entity entity) {
        ArrayList<String> pairs = new ArrayList<String>();
        try {
            Field[] allFields = entity.getClass().getDeclaredFields();
            for (Field field : allFields) {
                if (field.isAnnotationPresent(com.erc.dal.Field.class)) {
                    String name = ReflectionHelper.getDataBaseNameOfField(field);
                    if (HelperDataType.hasCuotes(field.getType())) {
                        pairs.add(Constant.PAIR_QUOTE.replaceFirst(Constant.FIELD, name).
                                replaceFirst(Constant.VALUE, Util.getValueFromField(field, entity)));
                    } else {
                        pairs.add(Constant.PAIR.replaceFirst(Constant.FIELD, name).
                                replaceFirst(Constant.VALUE, Util.getValueFromField(field, entity)));
                    }
                }
            }
        } catch (Exception e) {
            Log.e("Error: getPairs", e);
        }
        return TextUtils.join(",", pairs);
    }

    private static String getPairsToCreate(Class entity) {
        ArrayList<String> pairs = new ArrayList<>();
        try {
            Field[] allFields = entity.getDeclaredFields();
            for (Field field : allFields) {
                if (field.isAnnotationPresent(com.erc.dal.Field.class)) {
                    pairs.add(ReflectionHelper.getDataBaseNameOfField(field) + " " + HelperDataType.getDataBaseType(field.getType()));
                }
            }
        } catch (Exception e) {
            Log.e("Error: getPairs", e);
        }
        return TextUtils.join(",", pairs);
    }

    public static String geTableName(Class entity) {
        String res = "";
        try {
            Table table = (Table) entity.getAnnotation(Table.class);
            if (table != null) {
                if (table.name() == null) {
                    res = entity.getSimpleName();
                } else {
                    if (table.name().isEmpty()) {
                        res = entity.getSimpleName();
                    } else {
                        res = table.name();
                    }
                }
            } else {
                Log.e("Make sure that exist the annotation '@Table' on " + entity.getName(), null);
            }
        } catch (Exception e) {
            Log.e("Error: geTableName", e);
        }
        return res;
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
                    sb.append(Constant.CREATE.replaceFirst(Constant.TABLE, geTableName(entity)).
                            replaceFirst(Constant.FIELDS, getPairsToCreate(entity)));
                }
            } catch (ClassNotFoundException e) {
                Log.e("Error findSubClasses.ClassNotFoundException", e);
            }
        }

        return sb.toString();
    }

    public static String getAllQuery(Class entity) {
        String res = "";
        try {
            res = Constant.SELECT_FROM.replaceFirst("%t", geTableName(entity));
        } catch (Exception e) {
            Log.e("Error getAllQuery()", e);
        }
        return res;
    }

    public static String getQuery(Class classType, Object id) {
        StringBuffer sb = new StringBuffer();
        try {
            Pair pair = getPrimaryKey(classType, id);
            if (pair != null) {
                sb.append(Constant.SELECT_FROM);
                sb.append(Constant.WHERE);
                sb.append(pair.toString());
                sb.append(Constant.SEMICOLON);
                String table = geTableName(classType);
                return sb.toString().replaceAll(Constant.TABLE, table);
            }
        } catch (Exception e) {
            Log.e("Error getQuery()", e);
        }
        return null;
    }

    public static String getQueryRemove(Class entity, Object id) {
        StringBuffer sb = new StringBuffer();
        try {
            Pair pair = getPrimaryKey(entity, id);
            if (pair != null) {
                sb.append(Constant.DELETE.replaceFirst(Constant.KEYS, pair.toString()));
                String table = geTableName(entity);
                return sb.toString().replaceAll(Constant.TABLE, table);
            }
        } catch (Exception e) {
            Log.e("Error getQueryRemove()", e);
        }
        return null;
    }

    public static String getQueryInsert(Entity entity) {
        StringBuffer sb = new StringBuffer();
        try {
            NameValue namesValues = getNamesValues(entity);
            sb.append(Constant.INSERT.replaceFirst(Constant.FIELDS, namesValues.getName().toString()).
                    replaceFirst(Constant.VALUES, namesValues.getValue().toString()));
        } catch (Exception e) {
            Log.e("Error getQueryInsert()", e);
        }
        String table = geTableName(entity.getClass());
        return sb.toString().replaceAll(Constant.TABLE, table);
    }

    public static String getQueryUpdate(Entity entity) {
        StringBuffer sb = new StringBuffer();
        try {
            Pair pair = getPrimaryKey(entity);
            if (pair != null) {
                sb.append(Constant.UPDATE.replaceFirst(Constant.PAIRS, getPairs(entity)).
                        replaceFirst(Constant.KEYS, pair.toString()));
            }
        } catch (Exception e) {
            Log.e("Error getQueryUpdate()", e);
        }
        String table = geTableName(entity.getClass());
        return sb.toString().replaceAll(Constant.TABLE, table);
    }


}
