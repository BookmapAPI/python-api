package com.bookmap.api.rpc.server.data.income;

import com.bookmap.api.rpc.server.data.utils.AbstractEventWithAlias;
import com.bookmap.api.rpc.server.data.utils.Type;

public class AddPointToIndicatorEvent extends AbstractEventWithAlias {

	public final int indicatorId;
	public final double point;

	public AddPointToIndicatorEvent(String alias, int indicatorId, double point) {
		super(Type.ADD_POINT_TO_INDICATOR, alias);
		this.indicatorId = indicatorId;
		this.point = point;
	}
}
