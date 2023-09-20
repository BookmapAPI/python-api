package com.bookmap.api.rpc.server.handlers.indicators;

import com.bookmap.api.rpc.server.EventLoop;
import com.bookmap.api.rpc.server.State;
import com.bookmap.api.rpc.server.data.outcome.ErrorEvent;
import com.bookmap.api.rpc.server.data.outcome.IndicatorResponseEvent;
import com.bookmap.api.rpc.server.data.income.RegisterIndicatorEvent;
import com.bookmap.api.rpc.server.handlers.Handler;
import velox.api.layer1.simplified.Indicator;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

public class RegisterIndicatorHandler implements Handler<RegisterIndicatorEvent> {

	private static final AtomicInteger INDICATOR_ID_REGISTER = new AtomicInteger(42);
	private final ConcurrentMap<String, State> aliasToState;
	private final EventLoop eventLoop;

	public RegisterIndicatorHandler(ConcurrentMap<String, State> aliasToState, EventLoop eventLoop) {
		this.aliasToState = aliasToState;
		this.eventLoop = eventLoop;
	}

	@Override
	public void handle(RegisterIndicatorEvent event) {
		State state = aliasToState.getOrDefault(event.alias, null);
		if (state == null) {
			eventLoop.pushEvent(new ErrorEvent(event.alias, 1, "Instrument is not active", event.requestId));
			return;
		}

		Indicator indicator;
		if (event.isModifiable) {
			indicator = state.instrumentApi.registerIndicatorModifiable(
					event.name,
					event.graphType,
					event.initialValue,
					event.showLineByDefault,
					event.showWidgetByDefault
			);
		} else {
			indicator = state.instrumentApi.registerIndicator(
					event.name,
					event.graphType,
					event.initialValue,
					event.showWidgetByDefault,
					event.showWidgetByDefault
			);
		}
		Indicator finalIndicator = indicator;
		indicator.setColor(event.color);
		indicator.setLineStyle(event.lineStyle);
		aliasToState.compute(event.alias, (k, v) -> {
			if (v == null) {
				eventLoop.pushEvent(new ErrorEvent(event.alias, 1, "Unknown instrument", event.requestId));
				return null;
			}
			int id = INDICATOR_ID_REGISTER.getAndIncrement();
			v.aliasToIndicatorsAndTheirId.put(id, finalIndicator);
			eventLoop.pushEvent(new IndicatorResponseEvent(event.requestId, id));
			return v;
		});
	}
}
