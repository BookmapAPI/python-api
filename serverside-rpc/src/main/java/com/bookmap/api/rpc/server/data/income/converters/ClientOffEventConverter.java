package com.bookmap.api.rpc.server.data.income.converters;

import com.bookmap.api.rpc.server.data.utils.EventConverter;
import com.bookmap.api.rpc.server.data.income.ClientOffEvent;

import javax.inject.Inject;

public class ClientOffEventConverter implements EventConverter<String, ClientOffEvent> {

	@Inject
	public ClientOffEventConverter(){}

	@Override
	public ClientOffEvent convert(String entity) {
		return new ClientOffEvent();
	}
}
