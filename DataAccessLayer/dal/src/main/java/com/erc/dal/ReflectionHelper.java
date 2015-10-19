package com.erc.dal;

import java.lang.reflect.Constructor;
import java.util.ArrayList;

/**
 * Created by einar on 10/16/2015.
 */
public class ReflectionHelper {

    public static ArrayList<java.lang.reflect.Field> getFields(Object entity) {
        ArrayList<java.lang.reflect.Field> fields = new ArrayList<java.lang.reflect.Field>();
        try {
            java.lang.reflect.Field[] allFields = entity.getClass().getDeclaredFields();
            for (java.lang.reflect.Field field : allFields) {
                if (field.isAnnotationPresent(com.erc.dal.Field.class)) {
                    fields.add(field);
                }
            }
        } catch (Exception e) {
            Log.e("Error: getFields", e);
        }
        return fields;
    }

    public static Object getInstance(Class baseClass, Object[] paramsConstructor, Class[] paramsConstructorTypes) {
        Object object = null;
        try {
            Class<?> clazz = Class.forName(baseClass.getName());
            Constructor<?> ctor = clazz.getConstructor(paramsConstructorTypes);
            object = ctor.newInstance(paramsConstructor);
        } catch (Exception e) {
            Log.e("ReflectionHelper.getInstance", e);
        }
        return object;
    }

    public static String getDataBaseNameOfField(java.lang.reflect.Field field) {
        String res = "";
        com.erc.dal.Field key = (com.erc.dal.Field) field.getAnnotation(com.erc.dal.Field.class);
        if (key.name() == null) {
            res = field.getName();
        } else {
            if (key.name().isEmpty()) {
                res = field.getName();
            } else {
                res = key.name();
            }
        }
        return res;
    }

    public static String getFieldNameFromDBName(Entity entity, String fieldNameDB) {
        String res = "";
        try {
            java.lang.reflect.Field[] allFields = entity.getClass().getDeclaredFields();
            for (java.lang.reflect.Field field : allFields) {
                if (field.isAnnotationPresent(com.erc.dal.Field.class)) {
                    if (getDataBaseNameOfField(field).equals(fieldNameDB)) {
                        res = field.getName();
                        break;
                    }
                }
            }
        } catch (Exception e) {
            Log.e("Error: getFieldNameFromDBName", e);
        }
        return res;
    }
}
