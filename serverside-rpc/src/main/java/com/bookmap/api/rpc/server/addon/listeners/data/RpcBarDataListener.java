package com.bookmap.api.rpc.server.addon.listeners.data;

import com.bookmap.api.rpc.server.EventLoop;
import com.bookmap.api.rpc.server.data.BarEvent;
import velox.api.layer1.layers.utils.OrderBook;
import velox.api.layer1.simplified.Bar;
import velox.api.layer1.simplified.BarDataListener;

public class RpcBarDataListener implements BarDataListener {

	private final String alias;
	private final EventLoop eventLoop;
	private final int interval;

	public RpcBarDataListener(String alias, int interval, EventLoop eventLoop) {
		this.alias = alias;
		this.eventLoop = eventLoop;
		this.interval = interval;
	}

	@Override
	public void onBar(OrderBook orderBook, Bar bar) {
		BarEvent barEvent = new BarEvent(alias,
				bar.getOpen(),
				bar.getClose(),
				bar.getHigh(),
				bar.getLow(),
				bar.getVolumeBuy(),
				bar.getVolumeSell(),
				bar.getVolumeTotal(),
				bar.getVwap(),
				bar.getVwapSell(),
				bar.getVwapBuy()
		);

		eventLoop.pushEvent(barEvent);
	}

	@Override
	public long getInterval() {
		return interval;
	}
}
