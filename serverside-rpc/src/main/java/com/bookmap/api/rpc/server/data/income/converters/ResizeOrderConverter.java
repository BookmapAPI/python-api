package com.bookmap.api.rpc.server.data.income.converters;

import com.bookmap.api.rpc.server.data.income.ResizeOrderEvent;
import com.bookmap.api.rpc.server.data.utils.EventConverter;
import velox.api.layer1.data.OrderResizeParameters;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ResizeOrderConverter implements EventConverter<String, ResizeOrderEvent> {

    @Inject
    ResizeOrderConverter(){}

    @Override
    public ResizeOrderEvent convert(String entity) {
        String[] tokens = entity.split(FIELDS_DELIMITER);
        String alias = tokens[1];
        String orderId = tokens[2];
        int size = Integer.parseInt(tokens[3]);
        return new ResizeOrderEvent(alias, new OrderResizeParameters(orderId, size));
    }
}
