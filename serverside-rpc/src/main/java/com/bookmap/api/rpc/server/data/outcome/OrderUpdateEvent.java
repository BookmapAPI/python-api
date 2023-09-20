package com.bookmap.api.rpc.server.data.outcome;

import com.bookmap.api.rpc.server.data.utils.AbstractEvent;
import com.bookmap.api.rpc.server.data.utils.Type;
import velox.api.layer1.data.OrderInfoUpdate;

public class OrderUpdateEvent extends AbstractEvent {
    public final OrderInfoUpdate orderInfoUpdate;
    public OrderUpdateEvent(OrderInfoUpdate orderInfoUpdate) {
        super(Type.UPDATE_ORDER);
        this.orderInfoUpdate = orderInfoUpdate;
    }
}
