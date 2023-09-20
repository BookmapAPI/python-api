package com.bookmap.api.rpc.server.data.income;

import com.bookmap.api.rpc.server.data.utils.AbstractEvent;
import com.bookmap.api.rpc.server.data.utils.Type;
import velox.api.layer1.data.SimpleOrderSendParameters;

public class SendOrderEvent extends AbstractEvent {

    public final SimpleOrderSendParameters order;

    public SendOrderEvent(SimpleOrderSendParameters order) {
        super(Type.SEND_ORDER);
        this.order = order;
    }

    @Override
    public String toString() {
        return "SendOrderEvent{" +
                "order=" + order +
                ", type=" + type +
                '}';
    }
}
