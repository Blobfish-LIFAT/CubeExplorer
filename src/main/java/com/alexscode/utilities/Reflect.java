package com.alexscode.utilities;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class Reflect {

    public static Object getField(Object target, String fieldName){

        Field field = null;
        try {
            field = target.getClass().getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            return null;
        }

            field.setAccessible(true);


        try {
            return field.get(target);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }


}
