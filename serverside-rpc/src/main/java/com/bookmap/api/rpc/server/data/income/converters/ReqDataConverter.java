package com.bookmap.api.rpc.server.data.income.converters;

import com.bookmap.api.rpc.server.data.income.ReqBarDataEvent;
import com.bookmap.api.rpc.server.data.utils.EventConverter;
import com.bookmap.api.rpc.server.data.income.ReqDataEvent;
import com.bookmap.api.rpc.server.data.utils.Type;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ReqDataConverter implements EventConverter<String, ReqDataEvent> {

	@Inject
	public ReqDataConverter(){}

	@Override
	public ReqDataEvent convert(String entity) {
		String[] tokens = entity.split(String.valueOf(FIELDS_DELIMITER));
		String alias = tokens[1];
		long reqId = Long.parseLong(tokens[2]);
		Type requestedType = Type.of(Integer.parseInt(tokens[3]));

		switch (requestedType) {
			case BAR -> {
				return new ReqBarDataEvent(alias, reqId, Integer.parseInt(tokens[4]));
			}
			default -> {
				return new ReqDataEvent(requestedType, alias, reqId);
			}
		}
	}
}
