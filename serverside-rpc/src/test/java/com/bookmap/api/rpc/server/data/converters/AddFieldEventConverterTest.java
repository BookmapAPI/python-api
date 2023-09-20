package com.bookmap.api.rpc.server.data.converters;

import com.bookmap.api.rpc.server.data.income.AddUiField;
import com.bookmap.api.rpc.server.data.utils.Type;
import com.bookmap.api.rpc.server.data.income.converters.AddFieldEventConverter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.math.BigDecimal;

public class AddFieldEventConverterTest {

	private final AddFieldEventConverter addFieldEventConverter = new AddFieldEventConverter();

	@Test
	public void testAddStringFieldEventConversion() {
		String event = "14\uE000BTC-USDT\uE000STRING\uE000Test string\uE0001\uE000default value";

		var resultEntity = addFieldEventConverter.convert(event);

		Assertions.assertEquals(Type.ADD_SETTING_FIELD,resultEntity.type);
		Assertions.assertEquals("BTC-USDT", resultEntity.alias);
		Assertions.assertEquals(AddUiField.FieldType.STRING, resultEntity.fieldType);
		Assertions.assertEquals("Test string", resultEntity.name);
		Assertions.assertEquals("default value", resultEntity.defaultValue);
		Assertions.assertTrue( resultEntity.reloadIfChanged);
	}

	@Test
	public void testAddNumberFieldEventConversion() {
		String event = "14\uE000BTC-USDT\uE000NUMBER\uE000Test number\uE0001\uE00033.5\uE00010.0\uE00035.99\uE0000.01";

		var resultEntity = addFieldEventConverter.convert(event);

		Assertions.assertEquals(Type.ADD_SETTING_FIELD,resultEntity.type);
		Assertions.assertEquals("BTC-USDT", resultEntity.alias);
		Assertions.assertEquals(AddUiField.FieldType.NUMBER, resultEntity.fieldType);
		Assertions.assertEquals("Test number", resultEntity.name);
		Assertions.assertEquals(new BigDecimal("33.5"), resultEntity.defaultValue);
		Assertions.assertEquals(new BigDecimal("10.0"), resultEntity.minimum);
		Assertions.assertEquals(new BigDecimal("35.99"), resultEntity.maximum);
		Assertions.assertEquals(new BigDecimal("0.01"), resultEntity.step);
		Assertions.assertTrue( resultEntity.reloadIfChanged);
	}

	@Test
	public void testAddColorFiledEventConversion() {
		String event = "14\uE000BTC-USDT\uE000COLOR\uE000Test color\uE0001\uE000255,0,0";

		var resultEntity = addFieldEventConverter.convert(event);

		Assertions.assertEquals(Type.ADD_SETTING_FIELD,resultEntity.type);
		Assertions.assertEquals("BTC-USDT", resultEntity.alias);
		Assertions.assertEquals(AddUiField.FieldType.COLOR, resultEntity.fieldType);
		Assertions.assertEquals("Test color", resultEntity.name);
		Assertions.assertEquals(Color.RED, resultEntity.defaultValue);
		Assertions.assertTrue( resultEntity.reloadIfChanged);
	}

	@Test
	public void testAddBooleanFieldEventConversion() {
		String event = "14\uE000BTC-USDT\uE000BOOLEAN\uE000Test boolean\uE0001\uE0000";

		var resultEntity = addFieldEventConverter.convert(event);

		Assertions.assertEquals(Type.ADD_SETTING_FIELD,resultEntity.type);
		Assertions.assertEquals("BTC-USDT", resultEntity.alias);
		Assertions.assertEquals(AddUiField.FieldType.BOOLEAN, resultEntity.fieldType);
		Assertions.assertEquals("Test boolean", resultEntity.name);
		Assertions.assertFalse((boolean) resultEntity.defaultValue);
		Assertions.assertTrue( resultEntity.reloadIfChanged);
	}
}
