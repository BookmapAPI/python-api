package com.bookmap.api.rpc.server.data.income;

import com.bookmap.api.rpc.server.data.utils.Type;

public class ReqBarDataEvent extends ReqDataEvent {

	public final int barIntervalInSeconds;

	public ReqBarDataEvent(String alias, long requestId, int barIntervalInSeconds) {
		super(Type.BAR, alias, requestId);
		this.barIntervalInSeconds = barIntervalInSeconds;
	}
}
