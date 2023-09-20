package com.bookmap.api.rpc.server.data.income;

import com.bookmap.api.rpc.server.data.utils.AbstractEventWithAlias;
import com.bookmap.api.rpc.server.data.utils.Type;

import java.math.BigDecimal;

public class AddUiField extends AbstractEventWithAlias {

	public final FieldType fieldType;
	public final String name;
	public final Object defaultValue;
	public final BigDecimal step;
	public final BigDecimal minimum;
	public final BigDecimal maximum;
	public final boolean reloadIfChanged;

	public AddUiField(String alias, FieldType fieldType, String name, Object defaultValue, BigDecimal step, BigDecimal minimum, BigDecimal maximum, boolean reloadIfChanged) {
		super(Type.ADD_SETTING_FIELD, alias);
		this.fieldType = fieldType;
		this.name = name;
		this.defaultValue = defaultValue;
		this.step = step;
		this.minimum = minimum;
		this.maximum = maximum;
		this.reloadIfChanged = reloadIfChanged;
	}

	public AddUiField(String alias, FieldType fieldType, String name, Object defaultValue, boolean reloadIfChanged) {
		this(alias, fieldType, name, defaultValue, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, reloadIfChanged);
		if (fieldType == FieldType.NUMBER) {
			throw new IllegalStateException("Wrong constructor used, for number fields minimal and maximal values should be specified");
		}
	}

	public enum FieldType {
		NUMBER,
		COLOR,
		BOOLEAN,
		STRING;
	}
}
