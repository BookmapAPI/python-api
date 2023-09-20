package com.bookmap.api.rpc.server.handlers.indicators;

import com.bookmap.api.rpc.server.EventLoop;
import com.bookmap.api.rpc.server.State;
import com.bookmap.api.rpc.server.data.income.AddPointToIndicatorEvent;
import com.bookmap.api.rpc.server.data.outcome.ErrorEvent;
import com.bookmap.api.rpc.server.handlers.Handler;
import com.bookmap.api.rpc.server.log.RpcLogger;
import velox.api.layer1.simplified.Indicator;

import java.util.Map;

public class AddPointIndicatorHandler implements Handler<AddPointToIndicatorEvent> {

	private final Map<String, State> aliasToState;
	private final EventLoop eventLoop;

	public AddPointIndicatorHandler(Map<String, State> aliasToState, EventLoop eventLoop) {
		this.aliasToState = aliasToState;
		this.eventLoop = eventLoop;
	}

	@Override
	public void handle(AddPointToIndicatorEvent event) {
		RpcLogger.debug("Point: " + event.point);
		State state = aliasToState.get(event.alias);
		if (state == null) {
			RpcLogger.warn("No state for " + event.alias);
			eventLoop.pushEvent(new ErrorEvent(event.alias, 1, "Instrument is not active", -1));
			return;
		}

		int indicatorId = event.indicatorId;
		Indicator indicator = state.aliasToIndicatorsAndTheirId.get(indicatorId);
		if (indicator == null) {
			eventLoop.pushEvent(new ErrorEvent(event.alias, 2, "Unknown indicator with id " + indicatorId, -1));
			return;
		}
		indicator.addPoint(event.point);
	}
}
