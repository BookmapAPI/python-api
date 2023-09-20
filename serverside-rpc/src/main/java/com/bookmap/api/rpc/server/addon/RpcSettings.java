package com.bookmap.api.rpc.server.addon;

import com.bookmap.api.rpc.server.data.income.AddUiField;
import velox.api.layer1.settings.StrategySettingsVersion;

import java.awt.*;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@StrategySettingsVersion(currentVersion = 2, compatibleVersions = {})
public class RpcSettings {

	private final Map<String, SettingsParameter> params = new HashMap<>();

	public RpcSettings(){}

	/**
	 * Adds new parameter to the collection of parameters
	 * @param name name of the parameter, must be unique per settings set
	 * @param parameter parameter entity
	 */
	public void addParameter(String name, SettingsParameter parameter) throws IllegalArgumentException {
		if (params.containsKey(name)) {
			throw new IllegalArgumentException("Failed to add parameter with the name " + name + ". Parameter already exists.");
		}
		params.put(name, parameter);
	}

	/**
	 * Fetches parameter value by its name
	 * @param name name of parameter and unique identifier
	 * @return parameter value or null if parameter does not exist
	 */
	public SettingsParameter getParameter(String name) {
		return params.get(name);
	}

	public boolean containsParameter(String name) {
		return params.containsKey(name);
	}



	// TODO: Refactor to generics?
	public static class SettingsParameter {
		public final String name;
		public final AddUiField.FieldType type;
		// this value can be changed from UI, there is a high chance of concurrency issues including those go from UI
		private volatile Object value = null;

		// required by L1 API
		private SettingsParameter() {
			name = null;
			type = null;
		}

		public SettingsParameter(String name, AddUiField.FieldType type, Object value) {
			this.name = name;
			this.type = type;
			setValue(value);
		}

		public void setValue(Object value) {
			if (value == null) {
				throw new NullPointerException("Setting value can't be null");
			}
			switch (type) {
				case NUMBER ->{
					if (!(value instanceof BigDecimal) ) {
						throw new IllegalArgumentException("Number field must pe represented as BigDecimal value");
					}
				}
				case COLOR -> {
					if (!(value instanceof Color)) {
						throw new IllegalArgumentException("Wrong type of argument, expected - " + type + ", received - " + value.getClass().getCanonicalName());
					}
					var castedValue = (Color)value;
					this.value = Stream.of(castedValue.getRed(), castedValue.getGreen(), castedValue.getBlue()).collect(Collectors.toList());
					return;
				}
				case STRING -> {
					if (!(value instanceof String)) {
						throw new IllegalArgumentException("Wrong type of argument, expected - " + type + ", received - " + value.getClass().getCanonicalName());
					}
				}
				case BOOLEAN -> {
					if (!(value instanceof Boolean)) {
						throw new IllegalArgumentException("Wrong type of argument, expected - " + type + ", received - " + value.getClass().getCanonicalName());
					}
				}
			}
			this.value = value;
		}

		@SuppressWarnings("unchecked")
		public <T> T getValue(Class<T> clazz) {
			if (clazz == Color.class) {
				var rgb = ((List<Number>)value).stream().map(el -> (int)(double) el).collect(Collectors.toList());
				return clazz.cast(new Color(rgb.get(0), rgb.get(1), rgb.get(2)));
			}

			Object result = value;
			// Bookmap saves BigDecimals to a file as json numbers,
			// as a result when it is parse back, it is either Double or Long, so should
			// be cast back to BigDecimal correctly
			if (BigDecimal.class == clazz) {
				if (result instanceof Double) {
					return (T) BigDecimal.valueOf((double) result);
				}
				if (result instanceof Long) {
					return (T) BigDecimal.valueOf((long) result);
				}
			}

			return clazz.cast(result);
		}
	}
}
