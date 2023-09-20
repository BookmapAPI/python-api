package com.bookmap.api.rpc.server.data.income;

import com.bookmap.api.rpc.server.data.utils.Type;
import velox.api.layer1.data.OrderUpdateParameters;

public class MoveOrderEvent extends UpdateOrderEvent {
    public MoveOrderEvent(String alias, OrderUpdateParameters updateParameters) {
        super(Type.MOVE_ORDER, alias, updateParameters);
    }
}
