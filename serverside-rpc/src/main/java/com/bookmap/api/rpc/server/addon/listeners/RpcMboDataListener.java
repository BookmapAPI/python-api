package com.bookmap.api.rpc.server.addon.listeners;

import com.bookmap.api.rpc.server.EventLoop;
import com.bookmap.api.rpc.server.data.outcome.MboDataEvent;
import velox.api.layer1.simplified.MarketByOrderDepthDataListener;

public class RpcMboDataListener implements MarketByOrderDepthDataListener {

	private final String alias;
	private final EventLoop eventLoop;

	public RpcMboDataListener(String alias, EventLoop eventLoop) {
		this.alias = alias;
		this.eventLoop = eventLoop;
	}

	@Override
	public void send(String orderId, boolean isBid, int price, int size) {
		eventLoop.pushEvent(new MboDataEvent(alias, orderId, isBid ? MboDataEvent.MboDataType.BID_NEW : MboDataEvent.MboDataType.ASK_NEW, price, size));
	}

	@Override
	public void replace(String orderId, int price, int size) {
		eventLoop.pushEvent(new MboDataEvent(alias, orderId, MboDataEvent.MboDataType.REPLACE, price, size));
	}

	@Override
	public void cancel(String orderId) {
		eventLoop.pushEvent(new MboDataEvent(alias, orderId, MboDataEvent.MboDataType.CANCEL));
	}
}
