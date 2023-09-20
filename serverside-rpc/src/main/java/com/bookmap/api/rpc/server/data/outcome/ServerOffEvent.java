package com.bookmap.api.rpc.server.data.outcome;

import com.bookmap.api.rpc.server.data.utils.AbstractEvent;
import com.bookmap.api.rpc.server.data.utils.Type;

public class ServerOffEvent extends AbstractEvent {
	public ServerOffEvent() {
		super(Type.SERVER_OFF);
	}
}
