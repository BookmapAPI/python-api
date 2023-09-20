package com.bookmap.api.rpc.server.data.outcome.converters;

import com.bookmap.api.rpc.server.data.utils.EventConverter;
import com.bookmap.api.rpc.server.data.outcome.MboDataEvent;

import javax.inject.Inject;

public class MboDataEventConverter implements EventConverter<MboDataEvent, String> {

	@Inject
	public MboDataEventConverter() {}

	@Override
	public String convert(MboDataEvent entity) {
		return new StringBuilder()
				.append(entity.type.code)
				.append(FIELDS_DELIMITER)
				.append(entity.alias)
				.append(FIELDS_DELIMITER)
				.append(entity.mboEventType.name())
				.append(FIELDS_DELIMITER)
				.append(entity.orderId)
				.append(FIELDS_DELIMITER)
				.append(entity.price)
				.append(FIELDS_DELIMITER)
				.append(entity.size)
				.toString();
	}
}
