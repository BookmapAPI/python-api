package com.bookmap.api.rpc.server.utils;

import com.google.gson.JsonObject;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class JsonUtil {
    private static final Map<Class<?>, Field[]> classToFields = new ConcurrentHashMap<>();

    public static String convertObjectToJsonString(Object o) throws IllegalAccessException {
        JsonObject jsonObject = new JsonObject();
        Class<?> clazz = o.getClass();
        if (classToFields.containsKey(clazz)) {
            Field[] fields = classToFields.get(clazz);
            for (Field field : fields) {
                field.setAccessible(true);
                String fieldName = field.getName();
                Object fieldValue = field.get(o);
                jsonObject.addProperty(fieldName, fieldValue.toString());
            }
        } else {
            Field[] fields = clazz.getDeclaredFields();
            List<Field> fieldNames = new ArrayList<>();
            for (Field field : fields) {
                field.setAccessible(true);

                String fieldName = field.getName();
                if (fieldName.equals("serialVersionUID")) {
                    continue;
                }
                fieldNames.add(field);
                Object fieldValue = field.get(o);
                jsonObject.addProperty(fieldName, fieldValue.toString());
            }
            classToFields.put(clazz, fieldNames.toArray(new Field[0]));
        }

        return jsonObject.toString();
    }
}
