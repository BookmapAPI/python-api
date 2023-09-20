package com.bookmap.api.rpc.server.data.outcome.converters;

import com.bookmap.api.rpc.server.data.utils.EventConverter;
import com.bookmap.api.rpc.server.data.outcome.InstrumentInfoEvent;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class InstrumentInfoConverter implements EventConverter<InstrumentInfoEvent, String> {

	@Inject
	InstrumentInfoConverter() {}

	@Override
	public String convert(InstrumentInfoEvent entity) {
		StringBuilder builder = new StringBuilder();
		builder.append(entity.type.code);
		builder.append(FIELDS_DELIMITER);
		builder.append(entity.alias);
		builder.append(FIELDS_DELIMITER);
		builder.append(entity.fullName);
		builder.append(FIELDS_DELIMITER);
		builder.append(entity.isCrypto ? 1 : 0);
		builder.append(FIELDS_DELIMITER);
		builder.append(entity.pips);
		builder.append(FIELDS_DELIMITER);
		builder.append(entity.sizeMultiplier);
		builder.append(FIELDS_DELIMITER);
		builder.append(entity.instrumentMultiplier);
		return builder.toString();
	}
}
