package com.bookmap.api.rpc.server.data.outcome.converters;

import com.bookmap.api.rpc.server.data.utils.EventConverter;
import com.bookmap.api.rpc.server.data.outcome.ServerOffEvent;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ServerOffConverter implements EventConverter<ServerOffEvent, String> {

	@Inject
	public ServerOffConverter() {}

	@Override
	public String convert(ServerOffEvent entity) {
		return String.valueOf(entity.type.code);
	}
}
