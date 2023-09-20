package com.bookmap.api.rpc.server.data.outcome.converters;

import com.bookmap.api.rpc.server.data.utils.EventConverter;
import com.bookmap.api.rpc.server.data.outcome.TradeDataEvent;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class TradeDataConverter implements EventConverter<TradeDataEvent, String> {

	@Inject
	TradeDataConverter(){}

	@Override
	public String convert(TradeDataEvent entity) {
		var builder = new StringBuilder();
		builder.append(entity.type.code)
				.append(FIELDS_DELIMITER)
				.append(entity.alias)
				.append(FIELDS_DELIMITER)
				.append(entity.price)
				.append(FIELDS_DELIMITER)
				.append(entity.size)
				.append(FIELDS_DELIMITER)
				.append(entity.isOtc ? 1 : 0)
				.append(FIELDS_DELIMITER)
				.append(entity.isBidAggressor ? 1 : 0)
				.append(FIELDS_DELIMITER)
				.append(entity.isExecutionStart ? 1 : 0)
				.append(FIELDS_DELIMITER)
				.append(entity.isExecutionEnd ? 1 : 0)
				.append(FIELDS_DELIMITER)
				.append(entity.aggressorOrderId == null ? "" : entity.aggressorOrderId)
				.append(FIELDS_DELIMITER)
				.append(entity.passiveOrderId == null ? "" : entity.passiveOrderId);
		return builder.toString();
	}
}
