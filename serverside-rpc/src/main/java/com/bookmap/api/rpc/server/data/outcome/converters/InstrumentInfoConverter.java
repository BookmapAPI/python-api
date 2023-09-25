package com.bookmap.api.rpc.server.data.outcome.converters;

import com.bookmap.api.rpc.server.data.utils.EventConverter;
import com.bookmap.api.rpc.server.data.outcome.InstrumentInfoEvent;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import velox.api.layer1.data.Layer1ApiProviderSupportedFeatures;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class InstrumentInfoConverter implements EventConverter<InstrumentInfoEvent, String> {

	private final Gson gson;

	@Inject
	InstrumentInfoConverter() {
		GsonBuilder gsonBuilder = new GsonBuilder();
		gson = gsonBuilder.create();
	}

	@Override
	public String convert(InstrumentInfoEvent entity) {
		StringBuilder builder = new StringBuilder();
		JsonObject json = gson.toJsonTree(entity.supportedFeatures, Layer1ApiProviderSupportedFeatures.class).getAsJsonObject();

		json.remove("tradingVia");
		json.remove("tradingFrom");
		json.remove("knownInstruments");
		json.remove("lookupInfo");
		json.remove("pipsFunction");
		json.remove("sizeMultiplierFunction");
		json.remove("subscriptionInfoFunction");
		json.remove("historicalDataInfo");
		json.remove("receiveCrossTradingStatusMessage");
		json.remove("isHistoricalAggregationDisabled");

		builder.append(entity.type.code);
		builder.append(FIELDS_DELIMITER);
		builder.append(entity.alias);
		builder.append(FIELDS_DELIMITER);
		builder.append(entity.fullName);
		builder.append(FIELDS_DELIMITER);
		builder.append(entity.isCrypto ? 1 : 0);
		builder.append(FIELDS_DELIMITER);
		builder.append(entity.pips);
		builder.append(FIELDS_DELIMITER);
		builder.append(entity.sizeMultiplier);
		builder.append(FIELDS_DELIMITER);
		builder.append(entity.instrumentMultiplier);
		builder.append(FIELDS_DELIMITER);
		builder.append(json);

		return builder.toString();
	}
}
