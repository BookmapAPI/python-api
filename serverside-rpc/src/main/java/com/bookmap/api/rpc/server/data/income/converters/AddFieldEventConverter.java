package com.bookmap.api.rpc.server.data.income.converters;

import com.bookmap.api.rpc.server.data.utils.AbstractEvent;
import com.bookmap.api.rpc.server.data.utils.EventConverter;
import com.bookmap.api.rpc.server.data.income.AddUiField;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.*;
import java.math.BigDecimal;

@Singleton
public class AddFieldEventConverter implements EventConverter<String, AbstractEvent> {

	@Inject
	public AddFieldEventConverter(){}

	@Override
	public AddUiField convert(String entity) {
		String[] tokens = entity.split(FIELDS_DELIMITER);

		String alias = tokens[1];
		AddUiField.FieldType fieldType = AddUiField.FieldType.valueOf(tokens[2]);
		String name = tokens[3];
		boolean reloadOnChange = "1".equals(tokens[4]);
		Object defaultValue = convertDefaultFieldToObject(tokens[5], fieldType);
		if (AddUiField.FieldType.NUMBER == fieldType) {
			var minValue = new BigDecimal(tokens[6]);
			var maxValue = new BigDecimal(tokens[7]);
			var step = new BigDecimal(tokens[8]);
			return new AddUiField(alias,
					fieldType,
					name,
					defaultValue,
					step,
					minValue,
					maxValue,
					reloadOnChange
			);
		}
		return new AddUiField(alias, fieldType, name, defaultValue,reloadOnChange);
	}

	private Object convertDefaultFieldToObject(String defaultValToken, AddUiField.FieldType fieldType) {
		switch (fieldType) {
			case NUMBER -> {
				return new BigDecimal(defaultValToken);
			}
			case BOOLEAN -> {
				return "1".equals(defaultValToken);
			}
			case STRING -> {
				return defaultValToken;
			}
			case COLOR -> {
				var colorTokens = defaultValToken.split(",");
				return new Color(Integer.parseInt(colorTokens[0]), Integer.parseInt(colorTokens[1]), Integer.parseInt(colorTokens[2]));
			}
		}
		throw new RuntimeException("Unknown type received");
	}
}

