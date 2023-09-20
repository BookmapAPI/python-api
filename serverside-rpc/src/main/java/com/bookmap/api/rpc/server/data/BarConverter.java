package com.bookmap.api.rpc.server.data;

import com.bookmap.api.rpc.server.data.BarEvent;
import com.bookmap.api.rpc.server.data.utils.EventConverter;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class BarConverter implements EventConverter<BarEvent, String> {

	@Inject
	BarConverter() {}

	@Override
	public String convert(BarEvent entity) {
		StringBuilder builder = new StringBuilder();
		builder.append(entity.type.code);
		builder.append(FIELDS_DELIMITER);
		builder.append(entity.alias);
		builder.append(FIELDS_DELIMITER);
		builder.append(entity.high);
		builder.append(FIELDS_DELIMITER);
		builder.append(entity.low);
		builder.append(FIELDS_DELIMITER);
		builder.append(entity.open);
		builder.append(FIELDS_DELIMITER);
		builder.append(entity.close);
		builder.append(FIELDS_DELIMITER);
		builder.append(FIELDS_DELIMITER);
		builder.append(entity.volumeTotal);
		builder.append(entity.volumeBuy);
		builder.append(FIELDS_DELIMITER);
		builder.append(entity.volumeSell);
		builder.append(FIELDS_DELIMITER);
		builder.append(entity.vwap);
		builder.append(FIELDS_DELIMITER);
		builder.append(entity.vwapBuy);
		builder.append(FIELDS_DELIMITER);
		builder.append(entity.vwapSell);
		return builder.toString();
	}
}
