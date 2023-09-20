package com.bookmap.api.rpc.server.data.outcome;

import com.bookmap.api.rpc.server.data.utils.AbstractEventWithAlias;
import com.bookmap.api.rpc.server.data.utils.Type;

public class TradeDataEvent extends AbstractEventWithAlias {

	public final double price;
	public final int size;
	public final boolean isOtc;
	public final boolean isBidAggressor;
	public final boolean isExecutionStart;
	public final boolean isExecutionEnd;
	public final String aggressorOrderId;
	public final String passiveOrderId;

	public TradeDataEvent(String alias, double price, int size, boolean isOtc, boolean isBidAggressor, boolean isExecutionStart, boolean isExecutionEnd, String aggressorOrderId, String passiveOrderId) {
		super(Type.TRADE, alias);
		this.price = price;
		this.size = size;
		this.isOtc = isOtc;
		this.isBidAggressor = isBidAggressor;
		this.isExecutionStart = isExecutionStart;
		this.isExecutionEnd = isExecutionEnd;
		this.aggressorOrderId = aggressorOrderId;
		this.passiveOrderId = passiveOrderId;
	}
}
