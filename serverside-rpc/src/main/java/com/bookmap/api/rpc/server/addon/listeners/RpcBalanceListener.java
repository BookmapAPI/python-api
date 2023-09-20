package com.bookmap.api.rpc.server.addon.listeners;

import com.bookmap.api.rpc.server.EventLoop;
import com.bookmap.api.rpc.server.data.outcome.BalanceEvent;
import velox.api.layer1.data.BalanceInfo;
import velox.api.layer1.simplified.BalanceListener;

public class RpcBalanceListener implements BalanceListener {

    private final EventLoop eventLoop;

    public RpcBalanceListener(EventLoop eventLoop) {
        this.eventLoop = eventLoop;
    }

    @Override
    public void onBalance(BalanceInfo balanceInfo) {
        eventLoop.pushEvent(new BalanceEvent(balanceInfo));
    }
}
