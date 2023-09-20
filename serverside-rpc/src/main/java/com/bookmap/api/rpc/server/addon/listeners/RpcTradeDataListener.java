package com.bookmap.api.rpc.server.addon.listeners;

import com.bookmap.api.rpc.server.EventLoop;
import com.bookmap.api.rpc.server.data.outcome.TradeDataEvent;
import velox.api.layer1.data.TradeInfo;

public class RpcTradeDataListener implements velox.api.layer1.simplified.TradeDataListener {

	private final String alias;
	private final EventLoop eventLoop;

	public RpcTradeDataListener(String alias, EventLoop eventLoop) {
		this.alias = alias;
		this.eventLoop = eventLoop;
	}

	@Override
	public void onTrade(double price, int size, TradeInfo tradeInfo) {
		var tradeEvent = new TradeDataEvent(
				alias,
				price,
				size,
				tradeInfo.isOtc,
				tradeInfo.isBidAggressor,
				tradeInfo.isExecutionStart,
				tradeInfo.isExecutionEnd,
				tradeInfo.aggressorOrderId,
				tradeInfo.passiveOrderId
		);
		eventLoop.pushEvent(tradeEvent);
	}
}
