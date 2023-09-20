package com.bookmap.api.rpc.server.data.utils;

import com.bookmap.api.rpc.server.data.utils.exceptions.FailedToConvertException;
import com.bookmap.api.rpc.server.log.RpcLogger;

import javax.inject.Inject;
import java.util.Map;

public class IncomeConverterManager implements EventConverter<String, AbstractEvent> {

	private final Map<Type, EventConverter<String, ? extends AbstractEvent>> converters;

	@Inject
	public IncomeConverterManager(Map<Type, EventConverter<String, ? extends AbstractEvent>> converters) {
		this.converters = converters;
	}

	@Override
	public AbstractEvent convert(String event) {
		Type type = getTypeForEvent(event);
		if (!converters.containsKey(type)) {
			RpcLogger.error("Unknown message type " + type);
			throw new FailedToConvertException("Unknown event type: " + type);
		}

		var typeConverter = converters.get(type);

		try {
			return typeConverter.convert(event);
		} catch (Exception exception) {
			RpcLogger.error("Conversion of event failed", exception);
			throw new FailedToConvertException("Failed to convert event " + event);
		}
	}

	private Type getTypeForEvent(String event) {
		int upperBoundOfTheTypeField = event.indexOf('\uE000');
		if (upperBoundOfTheTypeField == -1) {
			return Type.of(Integer.parseInt(event));
		}

		return Type.of(Integer.parseInt(event.substring(0, upperBoundOfTheTypeField)));
	}
}
