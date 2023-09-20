package com.bookmap.api.rpc.server.data.outcome.converters;

import com.bookmap.api.rpc.server.data.outcome.OrderExecutionEvent;
import com.bookmap.api.rpc.server.data.utils.EventConverter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class OrderExecutionConverter implements EventConverter<OrderExecutionEvent, String> {

    private final Gson gson;

    @Inject
    OrderExecutionConverter() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        // It is specified here to avoid exceptions when some double values in the entity are NaN
        gsonBuilder.serializeSpecialFloatingPointValues();
        gson = gsonBuilder.create();
    }

    @Override
    public String convert(OrderExecutionEvent entity) {
        String json = gson.toJson(entity.executionInfo);

        StringBuilder builder = new StringBuilder();
        builder.append(entity.type.code)
                .append(FIELDS_DELIMITER)
                .append(entity.alias)
                .append(FIELDS_DELIMITER)
                .append(json);
        return builder.toString();
    }
}
