package com.bookmap.api.rpc.server.data.income.converters;

import com.bookmap.api.rpc.server.data.utils.AbstractEvent;
import com.bookmap.api.rpc.server.data.utils.EventConverter;
import com.bookmap.api.rpc.server.data.income.AddPointToIndicatorEvent;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class AddPointToIndicatorEventConverter implements EventConverter<String, AbstractEvent> {

	@Inject
	AddPointToIndicatorEventConverter(){}

	@Override
	public AddPointToIndicatorEvent convert(String entity) {
		String[] tokens = entity.split(FIELDS_DELIMITER);
		return new AddPointToIndicatorEvent(tokens[1], Integer.parseInt(tokens[2]), Double.parseDouble(tokens[3]));
	}
}
