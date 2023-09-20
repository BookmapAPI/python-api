package com.bookmap.api.rpc.server.data.outcome.converters;

import com.bookmap.api.rpc.server.data.utils.EventConverter;
import com.bookmap.api.rpc.server.data.outcome.ErrorEvent;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ErrorEventConverter implements EventConverter<ErrorEvent, String> {
    @Inject
    ErrorEventConverter() {}

    @Override
    public String convert(ErrorEvent entity) {
        // For now, it the only reason to send such message to client is not to get addon crash
        // TODO: implement error handler in python
        return entity.toString();
    }
}
