package com.bookmap.api.rpc.server.data.outcome;

import com.bookmap.api.rpc.server.data.utils.AbstractEventWithAlias;
import com.bookmap.api.rpc.server.data.utils.Type;

public class DepthDataEvent extends AbstractEventWithAlias {

	public final boolean isBid;
	public final int price;
	public final int size;

	public DepthDataEvent(String alias, boolean isBid, int price, int size) {
		super(Type.DEPTH, alias);
		this.isBid = isBid;
		this.price = price;
		this.size = size;
	}
}
