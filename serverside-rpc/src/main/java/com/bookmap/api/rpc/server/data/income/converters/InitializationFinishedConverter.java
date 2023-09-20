package com.bookmap.api.rpc.server.data.income.converters;

import com.bookmap.api.rpc.server.data.utils.EventConverter;
import com.bookmap.api.rpc.server.data.income.InitializationFinishedEvent;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class InitializationFinishedConverter implements EventConverter<String, InitializationFinishedEvent> {

	@Inject
	InitializationFinishedConverter(){}

	@Override
	public InitializationFinishedEvent convert(String entity) {
		String[] tokens = entity.split(FIELDS_DELIMITER);
		return new InitializationFinishedEvent(tokens[1]);
	}
}
