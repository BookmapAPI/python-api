package com.bookmap.api.rpc.server.data.utils;

import com.bookmap.api.rpc.server.data.utils.exceptions.FailedToConvertException;
import com.bookmap.api.rpc.server.log.RpcLogger;

import javax.inject.Inject;
import java.util.Map;

public class OutcomeConverterManager implements EventConverter<AbstractEvent, String> {

	private final Map<Type, EventConverter<? extends AbstractEvent, String>> converters;

	@Inject
	public OutcomeConverterManager(Map<Type, EventConverter<? extends AbstractEvent, String>> converters) {
		this.converters = converters;
	}

	@Override
	public String convert(AbstractEvent entity) {
		if (!converters.containsKey(entity.type)) {
			RpcLogger.warn("No converter for entity with type " + entity.type);
			throw new FailedToConvertException("Unknown type " + entity.type);
		}

		// crunchy approach to resolve type casting issue, TODO: find better solution
		var converter = (EventConverter<AbstractEvent, String>) converters.get(entity.type);
		String result = converter.convert(entity);
		return result;
	}
}
