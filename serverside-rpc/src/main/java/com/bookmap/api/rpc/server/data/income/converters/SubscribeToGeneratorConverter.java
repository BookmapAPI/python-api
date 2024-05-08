package com.bookmap.api.rpc.server.data.income.converters;

import com.bookmap.api.rpc.server.data.income.SubscribeToGeneratorEvent;
import com.bookmap.api.rpc.server.data.utils.AbstractEvent;
import com.bookmap.api.rpc.server.data.utils.EventConverter;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class SubscribeToGeneratorConverter implements EventConverter<String, AbstractEvent> {

    @Inject
    SubscribeToGeneratorConverter(){}

    @Override
    public SubscribeToGeneratorEvent convert(String entity) {
        String[] tokens = entity.split(FIELDS_DELIMITER);
        return new SubscribeToGeneratorEvent(tokens[1], tokens[2].equals("None") ? null : tokens[2], Boolean.parseBoolean(tokens[3]));
    }
}
