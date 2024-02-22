package com.bookmap.api.rpc.server.data.outcome.converters;

import com.bookmap.api.rpc.server.data.outcome.BroadcastingSettingsEvent;
import com.bookmap.api.rpc.server.data.utils.EventConverter;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class BroadcastingSettingsConverter implements EventConverter<BroadcastingSettingsEvent, String> {
    @Inject
    BroadcastingSettingsConverter() {}

    @Override
    public String convert(BroadcastingSettingsEvent entity) {
        return entity.type.code +
                FIELDS_DELIMITER +
                entity.generatorName +
                FIELDS_DELIMITER +
                entity.event;
    }
}
