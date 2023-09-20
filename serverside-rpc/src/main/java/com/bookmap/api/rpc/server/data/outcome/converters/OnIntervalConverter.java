package com.bookmap.api.rpc.server.data.outcome.converters;

import com.bookmap.api.rpc.server.data.utils.EventConverter;
import com.bookmap.api.rpc.server.data.outcome.OnIntervalEvent;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class OnIntervalConverter implements EventConverter<OnIntervalEvent, String> {

	@Inject
	OnIntervalConverter(){}

	@Override
	public String convert(OnIntervalEvent entity) {
		StringBuilder builder = new StringBuilder();
		builder.append(entity.type.code)
				.append(FIELDS_DELIMITER)
				.append(entity.alias);
		return builder.toString();
	}
}
