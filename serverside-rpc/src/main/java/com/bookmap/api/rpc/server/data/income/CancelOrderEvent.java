package com.bookmap.api.rpc.server.data.income;

import com.bookmap.api.rpc.server.data.utils.Type;
import velox.api.layer1.data.OrderUpdateParameters;

public class CancelOrderEvent extends UpdateOrderEvent {
    public CancelOrderEvent(String alias, OrderUpdateParameters updateParameters) {
        super(Type.CANCEL_ORDER, alias, updateParameters);
    }
}
