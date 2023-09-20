package com.bookmap.api.rpc.server.data.outcome.converters;

import com.bookmap.api.rpc.server.data.outcome.BalanceEvent;
import com.bookmap.api.rpc.server.data.utils.EventConverter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class BalanceConverter implements EventConverter<BalanceEvent, String> {

    private final Gson gson;

    @Inject
    BalanceConverter() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        // It is specified here to avoid exceptions when some double values in the entity are NaN
        gsonBuilder.serializeSpecialFloatingPointValues();
        gson = gsonBuilder.create();
    }

    @Override
    public String convert(BalanceEvent entity) {
        String json = gson.toJson(entity.balanceInfo);

        StringBuilder builder = new StringBuilder();
        builder.append(entity.type.code)
                .append(FIELDS_DELIMITER)
                .append(json);
        return builder.toString();
    }
}
