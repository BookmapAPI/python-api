package com.bookmap.api.rpc.server.data.income;

import com.bookmap.api.rpc.server.data.utils.AbstractEvent;
import com.bookmap.api.rpc.server.data.utils.Type;

public class ClientOffEvent extends AbstractEvent {

	public ClientOffEvent() {
		super(Type.CLIENT_OFF);
	}
}
