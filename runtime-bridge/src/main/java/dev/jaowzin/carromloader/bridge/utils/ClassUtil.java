package dev.jaowzin.carromloader.bridge.utils;

import dev.jaowzin.carromloader.bridge.annotation.BClass;
import dev.jaowzin.carromloader.bridge.annotation.BClassName;
import dev.jaowzin.carromloader.bridge.annotation.BClassNameNotProcess;


public class ClassUtil {
    public static Class<?> classReady(Class<?> clazz) {
        BClassNameNotProcess bClassNameNotProcess = clazz.getAnnotation(BClassNameNotProcess.class);
        if (bClassNameNotProcess != null) {
            return classReady(bClassNameNotProcess.value());
        }
        BClass annotation = clazz.getAnnotation(BClass.class);
        if (annotation != null) {
            return annotation.value();
        }
        BClassName bClassName = clazz.getAnnotation(BClassName.class);
        if (bClassName != null) {
            return classReady(bClassName.value());
        }
        return null;
    }

    private static Class<?> classReady(String clazz) {
        try {
            return Class.forName(clazz);
        } catch (ClassNotFoundException ignored) {
        }
        return null;
    }
}
