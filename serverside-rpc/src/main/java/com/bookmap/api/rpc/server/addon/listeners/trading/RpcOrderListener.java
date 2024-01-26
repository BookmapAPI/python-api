package com.bookmap.api.rpc.server.addon.listeners.trading;

import com.bookmap.api.rpc.server.EventLoop;
import com.bookmap.api.rpc.server.data.outcome.OrderExecutionEvent;
import com.bookmap.api.rpc.server.data.outcome.OrderUpdateEvent;
import velox.api.layer1.data.ExecutionInfo;
import velox.api.layer1.data.OrderInfoUpdate;
import velox.api.layer1.simplified.OrdersListener;

public class RpcOrderListener implements OrdersListener {

    private final String alias;
    private final EventLoop eventLoop;

    public RpcOrderListener(String alias, EventLoop eventLoop) {
        this.alias = alias;
        this.eventLoop = eventLoop;
    }

    @Override
    public void onOrderUpdated(OrderInfoUpdate orderInfoUpdate) {
        eventLoop.pushEvent(new OrderUpdateEvent(orderInfoUpdate));
    }

    @Override
    public void onOrderExecuted(ExecutionInfo executionInfo) {
        // ExecutionInfo doesn't contain alias by itself, so we put alias to the event here to maintain
        // understanding which the alias of execution is
        eventLoop.pushEvent(new OrderExecutionEvent(alias, executionInfo));
    }
}
