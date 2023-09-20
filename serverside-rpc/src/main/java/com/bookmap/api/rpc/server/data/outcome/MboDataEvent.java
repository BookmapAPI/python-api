package com.bookmap.api.rpc.server.data.outcome;

import com.bookmap.api.rpc.server.data.utils.AbstractEventWithAlias;
import com.bookmap.api.rpc.server.data.utils.Type;

public class MboDataEvent extends AbstractEventWithAlias {

	public final String orderId;
	public final MboDataType mboEventType;
	public final int price;
	public final int size;

	public MboDataEvent(String alias, String orderId, MboDataType mboEventType, int price, int size) {
		super(Type.MBO, alias);
		this.orderId = orderId;
		this.mboEventType = mboEventType;
		this.price = price;
		this.size = size;
	}

	public MboDataEvent(String alias, String orderId, MboDataType mboEventType) {
		this(alias, orderId, mboEventType, -1, -1);
	}

	public enum MboDataType {
		BID_NEW,
		ASK_NEW,
		REPLACE,
		CANCEL
	}
}
