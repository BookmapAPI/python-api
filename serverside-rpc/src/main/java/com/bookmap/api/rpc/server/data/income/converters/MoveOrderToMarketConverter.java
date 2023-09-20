package com.bookmap.api.rpc.server.data.income.converters;

import com.bookmap.api.rpc.server.data.income.MoveOrderToMarketEvent;
import com.bookmap.api.rpc.server.data.utils.EventConverter;
import velox.api.layer1.data.OrderMoveToMarketParameters;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class MoveOrderToMarketConverter implements EventConverter<String, MoveOrderToMarketEvent> {

    @Inject
    MoveOrderToMarketConverter(){}

    @Override
    public MoveOrderToMarketEvent convert(String entity) {
        String[] tokens = entity.split(FIELDS_DELIMITER);
        String alias = tokens[1];
        String orderId = tokens[2];
        int offset = Integer.parseInt(tokens[3]);
        return new MoveOrderToMarketEvent(alias, new OrderMoveToMarketParameters(orderId, offset));
    }
}
