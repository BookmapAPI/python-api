package com.bookmap.api.rpc.server.data.outcome.converters;

import com.bookmap.api.rpc.server.data.outcome.BroadcastingEvent;
import com.bookmap.api.rpc.server.data.utils.EventConverter;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class BroadcastingConverter implements EventConverter<BroadcastingEvent, String> {
    @Inject
    BroadcastingConverter() {}

    @Override
    public String convert(BroadcastingEvent entity) {
        return entity.type.code +
                FIELDS_DELIMITER +
                entity.alias +
                FIELDS_DELIMITER +
                entity.event;
    }
}
