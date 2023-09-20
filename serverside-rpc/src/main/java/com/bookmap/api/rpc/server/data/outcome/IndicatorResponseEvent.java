package com.bookmap.api.rpc.server.data.outcome;

import com.bookmap.api.rpc.server.data.utils.AbstractEvent;
import com.bookmap.api.rpc.server.data.utils.Type;

public class IndicatorResponseEvent extends AbstractEvent {

	public final long requestId;
	public final int indicatorId;

	public IndicatorResponseEvent(long requestId, int indicatorId) {
		super(Type.INDICATOR_RESPONSE);
		this.requestId = requestId;
		this.indicatorId = indicatorId;
	}
}
