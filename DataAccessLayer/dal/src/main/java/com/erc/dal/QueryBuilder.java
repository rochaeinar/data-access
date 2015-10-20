package com.erc.dal;

import android.content.Context;
import android.text.TextUtils;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Enumeration;

import dalvik.system.DexFile;

/**
 * Created by einar on 8/6/2015.
 */
public class QueryBuilder {

    public static void setID(Entity entity, DBConfig dbConfig) {
        try {
            Field[] fields = entity.getClass().getDeclaredFields();
            for (Field field : fields) {
                if (field.isAnnotationPresent(PrimaryKey.class)) {
                    long id = generateId(entity, dbConfig);
                    if (field.getType().equals(String.class)) {
                        field.set(entity, id + "");
                    } else {
                        field.set(entity, id);
                    }
                }
            }
        } catch (Exception e) {
            Log.e("Error: setID", e);
        }
    }

    private static long generateId(Entity entity, DBConfig dbConfig) {
        String nameId = getPrimaryKey(entity).getName();
        return entity.calculate(Aggregation.max(nameId)) + 1;
    }

    public static Pair getPrimaryKey(Entity entity) {
        Pair pair = new Pair();
        try {
            Field[] fields = entity.getClass().getDeclaredFields();
            for (Field field : fields) {
                if (field.isAnnotationPresent(PrimaryKey.class)) {
                    pair.setName(ReflectionHelper.getDataBaseNameOfField(field));
                    pair.setType(field.getType());
                    pair.setValue(Util.getValueFromField(field, entity));
                    break;
                }
            }
        } catch (Exception e) {
            Log.e("Error: getPrimaryKey", e);
        }
        return pair;
    }

    public static Pair getPrimaryKey(Class entity, Object id) {
        Pair pair = new Pair();
        try {
            Field[] fields = entity.getDeclaredFields();
            for (Field field : fields) {
                if (field.isAnnotationPresent(PrimaryKey.class)) {
                    pair.setName(ReflectionHelper.getDataBaseNameOfField(field));
                    pair.setType(field.getType());
                    pair.setValue(id.toString());
                    break;
                }
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
                            values.add(Ctt.VALUE_QUOTES.replaceFirst(Ctt.VALUE, Util.getValueFromField(field, entity)));
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
                        pairs.add(Ctt.PAIR_QUOTE.replaceFirst(Ctt.FIELD, name).
                                replaceFirst(Ctt.VALUE, Util.getValueFromField(field, entity)));
                    } else {
                        pairs.add(Ctt.PAIR.replaceFirst(Ctt.FIELD, name).
                                replaceFirst(Ctt.VALUE, Util.getValueFromField(field, entity)));
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
                Log.w("Invalid Entity class");
            }
        } catch (Exception e) {
            Log.e("Error: geTableName", e);
        }
        return res;
    }

    public static String getCreateQuery(Context context, Class type) {
        StringBuffer sb = new StringBuffer();
        String packageName = context.getPackageName();
        try {
            DexFile df = new DexFile(context.getPackageCodePath());
            for (Enumeration<String> iter = df.entries(); iter.hasMoreElements(); ) {
                String className = iter.nextElement();
                if (className.contains(packageName)) {
                    try {
                        Class entity = context.getClassLoader().loadClass(className);
                        if (entity.isAnnotationPresent(type)) {
                            sb.append(Ctt.CREATE.replaceFirst(Ctt.TABLE, geTableName(entity)).
                                    replaceFirst(Ctt.FIELDS, getPairsToCreate(entity)));
                        }
                    } catch (ClassNotFoundException e) {
                        Log.e("Error findSubClasses.ClassNotFoundException", e);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    public static String getAllQuery(Class entity) {
        String res = "";
        try {
            res = Ctt.SELECT_FROM.replaceFirst("%t", geTableName(entity));
        } catch (Exception e) {
            Log.e("Error getAllQuery()", e);
        }
        return res;
    }

    public static String getQuery(Class entity, long id) {
        StringBuffer sb = new StringBuffer();
        try {
            sb.append(Ctt.SELECT_FROM);
            sb.append(Ctt.WHERE);
            sb.append(getPrimaryKey(entity, id).toString());
            sb.append(Ctt.SEMICOLON);

        } catch (Exception e) {
            Log.e("Error getQuery()", e);
        }
        String table = geTableName(entity);
        return sb.toString().replaceAll(Ctt.TABLE, table);
    }

    public static String getQueryRemove(Class entity, int id) {
        StringBuffer sb = new StringBuffer();
        try {
            sb.append(Ctt.DELETE.replaceFirst(Ctt.KEYS, getPrimaryKey(entity, id).toString()));
        } catch (Exception e) {
            Log.e("Error getQueryRemove()", e);
        }
        String table = geTableName(entity);
        return sb.toString().replaceAll(Ctt.TABLE, table);
    }

    public static String getQueryInsert(Entity entity) {
        StringBuffer sb = new StringBuffer();
        try {
            NameValue namesValues = getNamesValues(entity);
            sb.append(Ctt.INSERT.replaceFirst(Ctt.FIELDS, namesValues.getName().toString()).
                    replaceFirst(Ctt.VALUES, namesValues.getValue().toString()));
        } catch (Exception e) {
            Log.e("Error getQueryInsert()", e);
        }
        String table = geTableName(entity.getClass());
        return sb.toString().replaceAll(Ctt.TABLE, table);
    }

    public static String getQueryUpdate(Entity entity) {
        StringBuffer sb = new StringBuffer();
        try {
            sb.append(Ctt.UPDATE.replaceFirst(Ctt.PAIRS, getPairs(entity)).
                    replaceFirst(Ctt.KEYS, getPrimaryKey(entity).toString()));
        } catch (Exception e) {
            Log.e("Error getQueryUpdate()", e);
        }
        String table = geTableName(entity.getClass());
        return sb.toString().replaceAll(Ctt.TABLE, table);
    }


}
