package com.bookmap.api.rpc.server.handlers;


import com.bookmap.api.rpc.server.EventLoop;
import com.bookmap.api.rpc.server.State;
import com.bookmap.api.rpc.server.data.income.UpdateOrderEvent;
import velox.api.layer1.common.Log;

import java.util.concurrent.ConcurrentMap;

public class UpdateOrderHandler implements Handler<UpdateOrderEvent> {

    private final ConcurrentMap<String, State> aliasToState;
    private final EventLoop eventLoop;

    public UpdateOrderHandler(ConcurrentMap<String, State> aliasToState, EventLoop eventLoop) {
        this.aliasToState = aliasToState;
        this.eventLoop = eventLoop;
    }

    @Override
    public void handle(UpdateOrderEvent event) {
        State state = aliasToState.get(event.alias);
        if (state == null) {
            Log.warn("The state for " + event.alias + " is null.");
            return;
        }
        state.instrumentApi.updateOrder(event.updateParameters);
    }
}
