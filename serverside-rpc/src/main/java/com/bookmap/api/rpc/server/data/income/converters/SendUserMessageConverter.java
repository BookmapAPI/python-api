package com.bookmap.api.rpc.server.data.income.converters;

import com.bookmap.api.rpc.server.data.income.SendUserMessageEvent;
import com.bookmap.api.rpc.server.data.utils.EventConverter;
import com.google.gson.JsonParser;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class SendUserMessageConverter implements EventConverter<String, SendUserMessageEvent> {

    private final JsonParser parser = new JsonParser();;

    @Inject
    SendUserMessageConverter(){}

    @Override
    public SendUserMessageEvent convert(String entity) {
        String[] fields = entity.split(FIELDS_DELIMITER);
        return new SendUserMessageEvent(fields[1], fields[2]);
    }
}
