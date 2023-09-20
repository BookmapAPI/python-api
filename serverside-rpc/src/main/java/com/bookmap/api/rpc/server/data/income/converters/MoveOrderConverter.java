package com.bookmap.api.rpc.server.data.income.converters;

import com.bookmap.api.rpc.server.data.income.MoveOrderEvent;
import com.bookmap.api.rpc.server.data.utils.EventConverter;
import velox.api.layer1.data.OrderMoveParameters;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class MoveOrderConverter implements EventConverter<String, MoveOrderEvent> {

    @Inject
    MoveOrderConverter(){}

    @Override
    public MoveOrderEvent convert(String entity) {
        String[] tokens = entity.split(FIELDS_DELIMITER);
        String alias = tokens[1];
        String orderId = tokens[2];
        double limitPrice = getDoubleValue(tokens[3]);
        double stopPrice = getDoubleValue(tokens[4]);
        return new MoveOrderEvent(alias, new OrderMoveParameters(orderId, stopPrice, limitPrice));
    }

    private double getDoubleValue(String token) {
        return token.equals("nan") ? Double.NaN : Double.parseDouble(token);
    }
}
