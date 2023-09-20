package com.bookmap.api.rpc.server.data.outcome.converters;

import com.bookmap.api.rpc.server.data.utils.EventConverter;
import com.bookmap.api.rpc.server.data.outcome.DepthDataEvent;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class DepthDataConverter implements EventConverter<DepthDataEvent, String> {

	@Inject
	DepthDataConverter(){}

	@Override
	public String convert(DepthDataEvent entity) {
		StringBuilder builder = new StringBuilder();
		builder.append(entity.type.code)
				.append(FIELDS_DELIMITER)
				.append(entity.alias)
				.append(FIELDS_DELIMITER)
				.append(entity.isBid ? 1 : 0)
				.append(FIELDS_DELIMITER)
				.append(entity.price)
				.append(FIELDS_DELIMITER)
				.append(entity.size);
		return builder.toString();
	}
}
