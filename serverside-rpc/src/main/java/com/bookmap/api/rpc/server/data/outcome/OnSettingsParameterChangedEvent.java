package com.bookmap.api.rpc.server.data.outcome;

import com.bookmap.api.rpc.server.data.utils.AbstractEventWithAlias;
import com.bookmap.api.rpc.server.data.utils.Type;
import com.bookmap.api.rpc.server.data.income.AddUiField;

public class OnSettingsParameterChangedEvent extends AbstractEventWithAlias {

	public final String name;
	public final AddUiField.FieldType parameterType;
	public final Object newValue;

	public OnSettingsParameterChangedEvent(String alias, String name, AddUiField.FieldType parameterType, Object newValue) {
		super(Type.ON_SETTINGS_PARAMETER_CHANGED, alias);
		this.name = name;
		this.parameterType = parameterType;
		this.newValue = newValue;
	}
}
