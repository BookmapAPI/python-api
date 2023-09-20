package com.bookmap.api.rpc.server.data.income;

import com.bookmap.api.rpc.server.data.utils.AbstractEventWithAlias;
import com.bookmap.api.rpc.server.data.utils.Type;

public class InitializationFinishedEvent extends AbstractEventWithAlias {

	public InitializationFinishedEvent(String alias) {
		super(Type.INITIALIZATION_FINISHED, alias);
	}
}
