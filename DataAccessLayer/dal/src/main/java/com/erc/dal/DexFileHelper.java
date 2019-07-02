package com.erc.dal;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Enumeration;

import dalvik.system.DexFile;

class DexFileHelper {
    public static ArrayList<String> findClassesStartWith(String prefix) {
        ArrayList<String> result = new ArrayList<>();
        try {
            ArrayList<DexFile> dexFiles = findAllDexFiles(Thread.currentThread().getContextClassLoader());
            for (DexFile dexFile : dexFiles) {
                if (dexFile != null) {
                    Enumeration<String> classNames = dexFile.entries();
                    while (classNames.hasMoreElements()) {
                        String className = classNames.nextElement();
                        if (className.startsWith(prefix)) {
                            result.add(className);
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return result;
    }

    private static ArrayList<DexFile> findAllDexFiles(ClassLoader classLoader) {
        ArrayList<DexFile> dexFiles = new ArrayList<>();
        try {
            java.lang.reflect.Field pathListField = findField(classLoader, "pathList");
            Object pathList = pathListField.get(classLoader);
            java.lang.reflect.Field dexElementsField = findField(pathList, "dexElements");
            Object[] dexElements = (Object[]) dexElementsField.get(pathList);
            java.lang.reflect.Field dexFileField = findField(dexElements[0], "dexFile");

            for (Object dexElement : dexElements) {
                Object dexFile = dexFileField.get(dexElement);
                dexFiles.add((DexFile) dexFile);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dexFiles;
    }

    private static java.lang.reflect.Field findField(Object instance, String name) throws NoSuchFieldException {
        Class clazz = instance.getClass();

        while (clazz != null) {
            try {
                Field field = clazz.getDeclaredField(name);
                if (!field.isAccessible()) {
                    field.setAccessible(true);
                }

                return field;
            } catch (NoSuchFieldException var4) {
                clazz = clazz.getSuperclass();
            }
        }

        throw new NoSuchFieldException("Field " + name + " not found in " + instance.getClass());
    }
}
