package com.bookmap.api.rpc.server.data.outcome;

import com.bookmap.api.rpc.server.data.utils.AbstractEvent;
import com.bookmap.api.rpc.server.data.utils.Type;
import com.bookmap.api.rpc.server.data.income.ClientInitEvent;

/**
 * Response of the server on {@link ClientInitEvent} meaning that server is ready to proceed.
 */
public class ServerInitEvent extends AbstractEvent {

	public ServerInitEvent() {
		super(Type.SERVER_INIT);
	}
}
