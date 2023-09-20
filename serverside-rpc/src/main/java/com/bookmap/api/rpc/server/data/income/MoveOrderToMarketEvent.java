package com.bookmap.api.rpc.server.data.income;

import com.bookmap.api.rpc.server.data.utils.Type;
import velox.api.layer1.data.OrderUpdateParameters;

public class MoveOrderToMarketEvent extends UpdateOrderEvent {
    // Currently, we don't want to support this type of updating orders
    public MoveOrderToMarketEvent(String alias, OrderUpdateParameters updateParameters) {
        super(Type.MOVE_ORDER_TO_MARKET, alias, updateParameters);
    }
}
