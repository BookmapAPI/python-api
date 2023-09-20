package com.bookmap.api.rpc.server.data.income.converters;

import com.bookmap.api.rpc.server.data.utils.EventConverter;
import com.bookmap.api.rpc.server.data.income.RegisterIndicatorEvent;
import com.bookmap.api.rpc.server.data.utils.exceptions.FailedToConvertException;
import com.bookmap.api.rpc.server.log.RpcLogger;
import velox.api.layer1.messages.indicators.Layer1ApiUserMessageModifyIndicator;
import velox.api.layer1.simplified.LineStyle;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.*;
import java.util.Arrays;
import java.util.stream.Collectors;

@Singleton
public class RegisterIndicatorConverter implements EventConverter<String, RegisterIndicatorEvent> {


	@Inject
	RegisterIndicatorConverter(){}

	@Override
	public RegisterIndicatorEvent convert(String entity) {
		try {
			String[] tokens = entity.split(FIELDS_DELIMITER);
			var color = Arrays.stream(tokens[8].split(",")).map(Integer::parseInt).collect(Collectors.toList());
			return new RegisterIndicatorEvent(tokens[1],
					Long.parseLong(tokens[2]),
					tokens[3],
					parseGraphType(tokens[4]),
					Double.parseDouble(tokens[5]),
					"1".equals(tokens[6]),
					"1".equals(tokens[7]),
					"1".equals(tokens[10]),
					new Color(color.get(0), color.get(1), color.get(2)),
					parseLineType(tokens[9])
			);
		} catch (Exception ex) {
			RpcLogger.error("Failed to convert RegisterIndicatorEvent", ex);
			throw new FailedToConvertException(ex.getMessage());
		}
	}

	private Layer1ApiUserMessageModifyIndicator.GraphType parseGraphType(String graphTypeToken) {
		switch (graphTypeToken) {
			case "PRIMARY" -> {
				return Layer1ApiUserMessageModifyIndicator.GraphType.PRIMARY;
			}
			case "BOTTOM" -> {
				return Layer1ApiUserMessageModifyIndicator.GraphType.BOTTOM;
			}
			case "NONE" -> {
				return Layer1ApiUserMessageModifyIndicator.GraphType.NONE;
			}
		}
		throw new IllegalStateException("Unknown graph type " + graphTypeToken);
	}

	private LineStyle parseLineType(String lineTypeToken) {
		switch (lineTypeToken) {
			case "SOLID" -> {
				return LineStyle.SOLID;
			}
			case "SHORT_DASH" -> {
				return LineStyle.SHORT_DASH;
			}
			case "LONG_DASH" -> {
				return LineStyle.LONG_DASH;
			}
			case "DOT" -> {
				return LineStyle.DOT;
			}
			case "DASH_DOT" -> {
				return LineStyle.DASH_DOT;
			}
		}
		throw new IllegalStateException("Unknown line type " + lineTypeToken);
	}
}
