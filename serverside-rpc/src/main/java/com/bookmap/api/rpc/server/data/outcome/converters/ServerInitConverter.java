package com.bookmap.api.rpc.server.data.outcome.converters;

import com.bookmap.api.rpc.server.data.utils.EventConverter;
import com.bookmap.api.rpc.server.data.outcome.ServerInitEvent;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ServerInitConverter implements EventConverter<ServerInitEvent, String> {

	@Inject
	ServerInitConverter(){}

	@Override
	public String convert(ServerInitEvent entity) {
		return String.valueOf(entity.type.code);
	}
}
