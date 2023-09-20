package com.bookmap.api.rpc.server.addon.listeners;

import com.bookmap.api.rpc.server.EventLoop;
import com.bookmap.api.rpc.server.data.outcome.DepthDataEvent;

public class RpcDepthDataListener implements velox.api.layer1.simplified.DepthDataListener {

	private final String alias;
	private final EventLoop eventLoop;

	public RpcDepthDataListener(String alias, EventLoop eventLoop) {
		this.alias = alias;
		this.eventLoop = eventLoop;
	}

	@Override
	public void onDepth(boolean isBid, int price, int size) {
		DepthDataEvent depthDataEvent = new DepthDataEvent(alias, isBid, price, size);
		eventLoop.pushEvent(depthDataEvent);
	}
}
