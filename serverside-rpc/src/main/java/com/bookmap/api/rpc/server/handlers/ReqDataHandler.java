package com.bookmap.api.rpc.server.handlers;

import com.bookmap.api.rpc.server.EventLoop;
import com.bookmap.api.rpc.server.State;
import com.bookmap.api.rpc.server.addon.listeners.data.RpcBarDataListener;
import com.bookmap.api.rpc.server.addon.listeners.data.RpcDepthDataListener;
import com.bookmap.api.rpc.server.addon.listeners.data.RpcMboDataListener;
import com.bookmap.api.rpc.server.addon.listeners.trading.RpcBalanceListener;
import com.bookmap.api.rpc.server.addon.listeners.trading.RpcOrderListener;
import com.bookmap.api.rpc.server.addon.listeners.trading.RpcPositionListener;
import com.bookmap.api.rpc.server.addon.listeners.trading.RpcTradeDataListener;
import com.bookmap.api.rpc.server.data.income.ReqBarDataEvent;
import com.bookmap.api.rpc.server.data.income.ReqDataEvent;
import com.bookmap.api.rpc.server.data.outcome.ErrorEvent;
import com.bookmap.api.rpc.server.data.outcome.RespDataEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentMap;

public class ReqDataHandler implements Handler<ReqDataEvent> {

	private final EventLoop eventLoop;
	private final Map<String, State> aliasToState;

	public ReqDataHandler(EventLoop eventLoop, ConcurrentMap<String, State> aliasToState) {
		this.eventLoop = eventLoop;
		this.aliasToState = aliasToState;
	}

	@Override
	public void handle(ReqDataEvent event) {
		State state = aliasToState.getOrDefault(event.alias, null);
		if (state == null) {
			eventLoop.pushEvent(new ErrorEvent(event.alias, 1, "Instrument is not active", event.requestId));
			return;
		}

		switch (event.typeOfRequestedData) {
			// TODO: alias to state collection can be changed from UI by user (or by bookmap crash??)
			// 	in this case all state related handlers should be synchronized with state itself
			case ORDER_INFO -> state.instrumentApi.addOrdersListeners(new RpcOrderListener(event.alias, eventLoop));
			case DEPTH -> state.instrumentApi.addDepthDataListeners(new RpcDepthDataListener(event.alias, eventLoop));
			case TRADE -> state.instrumentApi.addTradeDataListeners(new RpcTradeDataListener(event.alias, eventLoop));
			case BAR ->
					state.instrumentApi.addBarDataListeners(new RpcBarDataListener(event.alias, ((ReqBarDataEvent) event).barIntervalInSeconds, eventLoop));
			case MBO ->
					state.instrumentApi.addMarketByOrderDepthDataListeners(new RpcMboDataListener(event.alias, eventLoop));
			case BALANCE_UPDATE -> state.instrumentApi.addBalanceListeners(new RpcBalanceListener(eventLoop));
			case POSITION_UPDATE -> state.instrumentApi.addStatusListeners(new RpcPositionListener(eventLoop));
			default -> throw new IllegalStateException("Unknown type of requested data");
		}
		RespDataEvent respDataEvent = new RespDataEvent(event.requestId);
		eventLoop.pushEvent(respDataEvent);
	}
}
