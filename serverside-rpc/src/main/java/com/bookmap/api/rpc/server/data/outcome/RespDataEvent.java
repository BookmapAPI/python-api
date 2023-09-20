package com.bookmap.api.rpc.server.data.outcome;

import com.bookmap.api.rpc.server.data.utils.AbstractEvent;
import com.bookmap.api.rpc.server.data.utils.Type;

public class RespDataEvent extends AbstractEvent {

	public final long reqId;

	public RespDataEvent(long reqId) {
		super(Type.RESP_DATA);
		this.reqId = reqId;
	}
}
