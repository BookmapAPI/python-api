package com.bookmap.api.rpc.server.data.outcome;

import com.bookmap.api.rpc.server.data.utils.AbstractEventWithAlias;
import com.bookmap.api.rpc.server.data.utils.Type;

public class InstrumentDetachedEvent extends AbstractEventWithAlias {

	public InstrumentDetachedEvent(String alias) {
		super(Type.INSTRUMENT_DETACHED, alias);
	}
}
