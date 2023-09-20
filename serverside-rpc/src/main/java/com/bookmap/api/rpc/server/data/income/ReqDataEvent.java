package com.bookmap.api.rpc.server.data.income;

import com.bookmap.api.rpc.server.data.utils.AbstractEventWithAlias;
import com.bookmap.api.rpc.server.data.utils.Type;

public class ReqDataEvent extends AbstractEventWithAlias {

	public final Type typeOfRequestedData;
	public final long requestId;

	public ReqDataEvent(Type requestedDataType, String alias, long requestId) {
		super(Type.REQ_DATA, alias);
		this.typeOfRequestedData = requestedDataType;
		this.requestId = requestId;
	}
}
