package com.bookmap.api.rpc.server.addon.listeners.trading;

import com.bookmap.api.rpc.server.EventLoop;
import com.bookmap.api.rpc.server.data.outcome.PositionUpdateEvent;
import velox.api.layer1.data.StatusInfo;
import velox.api.layer1.simplified.PositionListener;

public class RpcPositionListener implements PositionListener {

    private final EventLoop eventLoop;

    public RpcPositionListener(EventLoop eventLoop) {
        this.eventLoop = eventLoop;
    }

    @Override
    public void onPositionUpdate(StatusInfo statusInfo) {
        eventLoop.pushEvent(new PositionUpdateEvent(statusInfo));
    }
}
