package com.bookmap.api.rpc.server.data.income;

import com.bookmap.api.rpc.server.data.utils.Type;
import velox.api.layer1.data.OrderUpdateParameters;

public class ResizeOrderEvent extends UpdateOrderEvent {
    public ResizeOrderEvent(String alias, OrderUpdateParameters updateParameters) {
        super(Type.RESIZE_ORDER, alias, updateParameters);
    }
}
