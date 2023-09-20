package com.bookmap.api.rpc.server.handlers;


import com.bookmap.api.rpc.server.EventLoop;
import com.bookmap.api.rpc.server.State;
import com.bookmap.api.rpc.server.data.income.SendOrderEvent;
import velox.api.layer1.common.Log;

import java.util.concurrent.ConcurrentMap;

public class SendOrderHandler implements Handler<SendOrderEvent> {

    private final ConcurrentMap<String, State> aliasToState;
    private final EventLoop eventLoop;

    public SendOrderHandler(ConcurrentMap<String, State> aliasToState, EventLoop eventLoop) {
        this.aliasToState = aliasToState;
        this.eventLoop = eventLoop;
    }

    @Override
    public void handle(SendOrderEvent event) {
        State state = aliasToState.get(event.order.alias);
        if (state == null) {
            Log.warn("The state for " + event.order.alias + " is null.");
            return;
        }
        state.instrumentApi.sendOrder(event.order);
    }
}
