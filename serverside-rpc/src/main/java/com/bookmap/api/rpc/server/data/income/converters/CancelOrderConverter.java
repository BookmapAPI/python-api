package com.bookmap.api.rpc.server.data.income.converters;

import com.bookmap.api.rpc.server.data.income.CancelOrderEvent;
import com.bookmap.api.rpc.server.data.utils.EventConverter;
import velox.api.layer1.data.OrderCancelParameters;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class CancelOrderConverter implements EventConverter<String, CancelOrderEvent> {

    @Inject
    CancelOrderConverter(){}

    @Override
    public CancelOrderEvent convert(String entity) {
        String[] tokens = entity.split(FIELDS_DELIMITER);
        String alias = tokens[1];
        String orderId = tokens[2];
        boolean isBatchEnd = Boolean.parseBoolean(tokens[3]);
        if ("nan".equals(tokens[4])) {
            return new CancelOrderEvent(alias, new OrderCancelParameters(orderId, isBatchEnd));
        }
        long batchId = Long.parseLong(tokens[4]);
        return new CancelOrderEvent(alias, new OrderCancelParameters(orderId, batchId, isBatchEnd));
    }
}
