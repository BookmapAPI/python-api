package com.bookmap.api.rpc.server.data.outcome;

import com.bookmap.api.rpc.server.data.utils.AbstractEvent;
import com.bookmap.api.rpc.server.data.utils.AbstractEventWithAlias;
import com.bookmap.api.rpc.server.data.utils.Type;

public class OnIntervalEvent extends AbstractEventWithAlias {

	public OnIntervalEvent(String alias) {
		super(Type.ON_INTERVAL, alias);
	}
}
