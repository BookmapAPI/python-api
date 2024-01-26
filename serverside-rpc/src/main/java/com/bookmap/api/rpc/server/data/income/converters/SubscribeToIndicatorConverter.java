package com.bookmap.api.rpc.server.data.income.converters;

import com.bookmap.api.rpc.server.data.income.SubscribeToIndicatorEvent;
import com.bookmap.api.rpc.server.data.utils.AbstractEvent;
import com.bookmap.api.rpc.server.data.utils.EventConverter;

import javax.inject.Inject;
import javax.inject.Singleton;

import static com.bookmap.api.rpc.server.data.utils.EventConverter.FIELDS_DELIMITER;

@Singleton
public class SubscribeToIndicatorConverter implements EventConverter<String, AbstractEvent> {

    @Inject
    SubscribeToIndicatorConverter(){}

    @Override
    public SubscribeToIndicatorEvent convert(String entity) {
        String[] tokens = entity.split(FIELDS_DELIMITER);
        return new SubscribeToIndicatorEvent(tokens[1], tokens[2]);
    }
}
