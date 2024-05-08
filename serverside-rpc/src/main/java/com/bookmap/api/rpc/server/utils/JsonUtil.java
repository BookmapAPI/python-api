package com.bookmap.api.rpc.server.utils;

import com.google.gson.*;

public class JsonUtil {
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(Class.class, (JsonSerializer<Class<?>>) (src, typeOfSrc, context) -> null).create();

    public static String convertObjectToJsonString(Object o) {
        return gson.toJson(o);
    }
}
