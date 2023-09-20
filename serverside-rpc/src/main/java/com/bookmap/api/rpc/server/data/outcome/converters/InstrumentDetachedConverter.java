package com.bookmap.api.rpc.server.data.outcome.converters;

import com.bookmap.api.rpc.server.data.utils.EventConverter;
import com.bookmap.api.rpc.server.data.outcome.InstrumentDetachedEvent;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class InstrumentDetachedConverter implements EventConverter<InstrumentDetachedEvent, String> {

	@Inject
	InstrumentDetachedConverter(){}

	@Override
	public String convert(InstrumentDetachedEvent entity) {
		return entity.type.code + FIELDS_DELIMITER + entity.alias;
	}
}
