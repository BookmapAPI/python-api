package com.bookmap.api.rpc.server.data.outcome.converters;

import com.bookmap.api.rpc.server.data.utils.EventConverter;
import com.bookmap.api.rpc.server.data.outcome.IndicatorResponseEvent;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class IndicatorResponseConverter implements EventConverter<IndicatorResponseEvent, String> {

	@Inject
	IndicatorResponseConverter(){}

	@Override
	public String convert(IndicatorResponseEvent entity) {
		return new StringBuilder()
				.append(entity.type.code)
				.append(FIELDS_DELIMITER)
				.append(entity.requestId)
				.append(FIELDS_DELIMITER)
				.append(entity.indicatorId)
				.toString();
	}
}
