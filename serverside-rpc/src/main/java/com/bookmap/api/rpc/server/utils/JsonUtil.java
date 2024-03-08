package com.bookmap.api.rpc.server.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class JsonUtil {
    private static final Map<Class<?>, Field[]> classToFields = new ConcurrentHashMap<>();

    private static final Gson gson = new GsonBuilder().create();

    public static String convertObjectToJsonString(Object o) throws IllegalAccessException {
        JsonObject jsonObject = new JsonObject();
//        ObjectMapper objectMapper = new ObjectMapper();
//        ObjectNode objectNode = objectMapper.createObjectNode();
//
//        JsonNode fieldValueJson = objectMapper.valueToTree(o);
//        System.out.println(fieldValueJson);
//        try {
//            String json = objectMapper.writeValueAsString(o);
//        } catch (JsonProcessingException e) {
//            throw new RuntimeException(e);
//        }
        Class<?> clazz = o.getClass();
        if (classToFields.containsKey(clazz)) {
            Field[] fields = classToFields.get(clazz);
            for (Field field : fields) {
                field.setAccessible(true);
                String fieldName = field.getName();
                Object fieldValue = field.get(o);
                jsonObject.addProperty(fieldName, getStringValue(fieldValue, field));
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
                jsonObject.addProperty(fieldName, getStringValue(fieldValue, field));
            }
            classToFields.put(clazz, fieldNames.toArray(new Field[0]));
        }

        return jsonObject.toString();
    }

    private static String getStringValue(Object fieldValue, Field field) {
        if (fieldValue != null) {
            if (field.getType().equals(Map.class)) {
                JsonObject mapJson = new JsonObject();
                Map<?, ?> mapValue = (Map<?, ?>) fieldValue;
                for (Map.Entry<?, ?> entry : mapValue.entrySet()) {
                    mapJson.addProperty(entry.getKey().toString(), entry.getValue().toString());
                }
                return mapJson.toString();
            } else {
                return fieldValue.toString();
            }
        } else {
            return null;
        }
    }
}
