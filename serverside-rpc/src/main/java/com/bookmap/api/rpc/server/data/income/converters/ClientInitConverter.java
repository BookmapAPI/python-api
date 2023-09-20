package com.bookmap.api.rpc.server.data.income.converters;

import com.bookmap.api.rpc.server.data.utils.AbstractEvent;
import com.bookmap.api.rpc.server.data.utils.EventConverter;
import com.bookmap.api.rpc.server.data.income.ClientInitEvent;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ClientInitConverter implements EventConverter<String, AbstractEvent> {

	@Inject
	ClientInitConverter(){}

	@Override
	public ClientInitEvent convert(String entity) {
		return new ClientInitEvent();
	}
}
