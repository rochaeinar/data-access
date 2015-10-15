package com.erc.dal;

import java.util.Date;

/**
 * Created by einar on 9/5/2015.
 */
public class Util {
    public static String getValueFromField(java.lang.reflect.Field field, Object entity) {
        String res = "";
        try {
            Object value = field.get(entity);
            if (value != null) {
                res = getValueFromObject(value);
            } else {
                Log.w("null value on getValueFromField: " + entity.getClass().getName() + "." + field.getName());
            }
        } catch (Exception e) {
            Log.e("Error getValueFromField", e);
        }
        return res;
    }

    public static String getValueFromObject(Object value) {
        String res = "";
        try {
            if (value != null) {
                do {
                    if (value.getClass().equals(Boolean.class)) {
                        value = ((Boolean) value).booleanValue() ? "1" : "0";
                        break;
                    }
                    if (value.getClass().equals(Character.class)) {
                        value = ((Character) value).equals('\0') ? "" : "" + ((Character) value).charValue();
                        break;
                    }
                    if (value.getClass().equals(Date.class)) {
                        value = HelperDate.getDateWithFormat(((Date) value), HelperDate.ISO_FORMAT);
                        break;
                    }
                    break;
                } while (true);
                res = value.toString();
            } else {
                Log.w("null value on getValueFromObject");
            }
        } catch (Exception e) {
            Log.e("Error getValueFromObject", e);
        }
        return res;
    }

    public static boolean isNullOrEmpty(String text) {
        return (text == null || text.trim().equals("null") || text.trim().length() <= 0);
    }
}
