package com.bookmap.api.rpc.server.data.income;

import com.bookmap.api.rpc.server.data.utils.AbstractEvent;
import com.bookmap.api.rpc.server.data.outcome.ServerInitEvent;
import com.bookmap.api.rpc.server.data.utils.Type;

/**
 * Event sent by the client in the beginning of a communication meaning that client
 * is ready and expects server to be ready as well. Response for this event is {@link ServerInitEvent}
 */
public class ClientInitEvent extends AbstractEvent {

	public ClientInitEvent() {
		super(Type.CLIENT_INIT);
	}
}
