package com.bookmap.api.rpc.server.data.outcome.converters;

import com.bookmap.api.rpc.server.data.utils.EventConverter;
import com.bookmap.api.rpc.server.data.outcome.RespDataEvent;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class RespDataConverter implements EventConverter<RespDataEvent, String> {

	@Inject
	RespDataConverter(){}

	@Override
	public String convert(RespDataEvent entity) {
		return entity.type.code + FIELDS_DELIMITER + entity.reqId;
	}
}
