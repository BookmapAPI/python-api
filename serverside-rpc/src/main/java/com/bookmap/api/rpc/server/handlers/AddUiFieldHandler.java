package com.bookmap.api.rpc.server.handlers;

import com.bookmap.api.rpc.server.EventLoop;
import com.bookmap.api.rpc.server.State;
import com.bookmap.api.rpc.server.addon.RpcSettings;
import com.bookmap.api.rpc.server.data.income.AddUiField;
import com.bookmap.api.rpc.server.data.outcome.OnSettingsParameterChangedEvent;
import com.bookmap.api.rpc.server.log.RpcLogger;
import com.bookmap.api.rpc.server.ui.listeners.DocumentListenerAdapter;
import velox.api.layer1.common.Log;
import velox.gui.StrategyPanel;
import velox.gui.colors.ColorsConfigItem;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AddUiFieldHandler implements Handler<AddUiField> {

	private final Map<String, State> aliasToState;
	private final EventLoop eventLoop;

	private final Map<String, JButton> aliasToApplySettingsButton = new HashMap<>();

	private final Map<String, PendingParameterChanges> parameterNameToPendingChanges = new ConcurrentHashMap<>();

	public AddUiFieldHandler(Map<String, State> aliasToState, EventLoop eventLoop) {
		this.aliasToState = aliasToState;
		this.eventLoop = eventLoop;
	}

	@Override
	public void handle(AddUiField event) {
		SwingUtilities.invokeLater(() -> {
			var state = aliasToState.get(event.alias);
			initSettingsIfNotInited(event.alias, state);
			var settings = state.settings;
			try {
				// TODO: check that parameter is the same
				if (!settings.containsParameter(event.name)) {
					state.settings.addParameter(event.name, new RpcSettings.SettingsParameter(event.name, event.fieldType, event.defaultValue));
				}
			} catch (Exception ex) {
				RpcLogger.warn("Failed to add UI parameter", ex);
				//TODO: error sent
			}
			switch (event.fieldType) {
				case NUMBER -> addNumberField(state, event);
				case STRING -> addStringField(state, event);
				case BOOLEAN -> addBooleanField(state, event);
				case COLOR -> addColorField(state, event);
				default -> Log.warn("Unknown field type " + event.fieldType);
			}
		});
	}

	private void initSettingsIfNotInited(String alias, State state) {
		if (state.settingsConfig == null) {
			state.settingsConfig = new StrategyPanel("Settings", new GridBagLayout());
			var applySettings = new JButton("Apply");
			aliasToApplySettingsButton.put(alias, applySettings);
			applySettings.setEnabled(false);
			applySettings.addActionListener((e) -> {
				parameterNameToPendingChanges.forEach((name, update) ->
						setNewSettingsValue(update.alias, state, update.name, update.type, update.newValue, true));
				parameterNameToPendingChanges.clear();
			});
			state.settingsConfig.add(applySettings, getButtonConstraints());
		}
		if (state.colorsConfig == null) {
			state.colorsConfig = new StrategyPanel("Colors", new GridBagLayout());
		}
	}

	private void addColorField(State state, AddUiField event) {
		var settings = state.settings;
		var parameterName = event.name;
		var initialColor = (Color) (settings.containsParameter(parameterName) ? settings.getParameter(parameterName).getValue(Color.class) : event.defaultValue);
		var colorItem = new ColorsConfigItem(initialColor, (Color) event.defaultValue,
				(color) ->
						eventLoop.pushEvent(
								new OnSettingsParameterChangedEvent(
										event.alias,
										event.name,
										AddUiField.FieldType.COLOR,
										color)
						)
		);

		var colorLabel = new JLabel(parameterName);
		state.colorsConfig.add(colorLabel, getConstraintsForColors(0, getNextRowForComponentInColorsPanel(state)));
		state.colorsConfig.add(colorItem, getConstraintsForColors(1, getNextRowForComponentInColorsPanel(state)));

	}

	private void addBooleanField(State state, AddUiField event) {
		var checkBox = new JCheckBox("");
		var label = new JLabel(event.name);
		var panel = state.settingsConfig;
		panel.add(label, getConstraints(0, getNextRowForComponentInSettingPanel(state)));
		panel.add(checkBox, getConstraints(1, getNextRowForComponentInSettingPanel(state)));

		// set settings

		var isBoxSelected = (Boolean) event.defaultValue;
		if (state.settings.containsParameter(event.name)) {
			isBoxSelected = state.settings.getParameter(event.name).getValue(Boolean.class);
		}

		checkBox.setSelected(isBoxSelected);
		setNewSettingsValue(event.alias, state, event.name, event.fieldType, isBoxSelected, true);
		checkBox.addChangeListener(e -> setNewSettingsValue(event.alias, state, event.name, event.fieldType, checkBox.isSelected(), false));
	}

	private void addStringField(State state, AddUiField event) {
		var textField = new JTextField();
		var label = new JLabel(event.name);
		var text = (String) event.defaultValue;
		if (state.settings.containsParameter(event.name)) {
			text = state.settings.getParameter(event.name).getValue(String.class);
		}
		textField.setText(text);
		var settingsField = state.settingsConfig;
		settingsField.add(label, getConstraints(0, getNextRowForComponentInSettingPanel(state)));
		settingsField.add(textField, getConstraints(1, getNextRowForComponentInSettingPanel(state)));
		setNewSettingsValue(event.alias, state, event.name, event.fieldType, text, true);
		textField.getDocument().addDocumentListener(new DocumentListenerAdapter() {
			@Override
			public void onReplace(String newText) {
				setNewSettingsValue(event.alias, state, event.name, event.fieldType, newText, false);
			}
		});
	}

	private void addNumberField(State state, AddUiField event) {
		StrategyPanel panel = state.settingsConfig;
		RpcSettings settings = state.settings;
		String settingParameterName = event.name;

		RpcSettings.SettingsParameter parameter = settings.getParameter(settingParameterName);
		// take initial value either from saved settings or from user's specified value
		BigDecimal defaultValue = (BigDecimal) (parameter == null ? event.defaultValue : parameter.getValue(BigDecimal.class));
		setNewSettingsValue(event.alias, state, settingParameterName, event.fieldType, defaultValue, true);

		var label = new JLabel(settingParameterName);
		var sModel = new SpinnerNumberModel(
				defaultValue.doubleValue(), event.minimum.doubleValue(), event.maximum.doubleValue(), event.step.doubleValue()
		);
		sModel.addChangeListener(e -> setNewSettingsValue(event.alias,
				state,
				settingParameterName,
				event.fieldType,
				BigDecimal.valueOf((double) sModel.getValue()),
				false)
		);
		panel.add(label, getConstraints(0, getNextRowForComponentInSettingPanel(state)));

		var numberSpinner = new JSpinner(sModel);
		// ban spinner from being changed through editor, taken here: https://stackoverflow.com/questions/2902101/how-to-set-jspinner-as-non-editable
		((JSpinner.DefaultEditor) numberSpinner.getEditor()).getTextField().setEditable(false);

		panel.add(numberSpinner, getConstraints(1, getNextRowForComponentInSettingPanel(state)));
		panel.requestReload();
	}

	// taken with small changes from SimplifiedApiWrapper project
	private GridBagConstraints getConstraints(int x, int y) {
		GridBagConstraints gbConst = new GridBagConstraints();
		setDefaultConstraints(gbConst, x, y);

		if (x == 1) {
			gbConst.anchor = GridBagConstraints.EAST;
			gbConst.weightx = 0.26;
			gbConst.fill = GridBagConstraints.HORIZONTAL;
		}
		return gbConst;
	}

	private void setNewSettingsValue(String alias, State state, String parameterName, AddUiField.FieldType fieldType, Object newValue, boolean writeChange) {
		RpcSettings rpcSettings = state.settings;
		RpcSettings.SettingsParameter parameter = rpcSettings.getParameter(parameterName);
		if (parameter == null) {
			RpcLogger.info("Unknown parameter " + parameterName + " type " + fieldType);
			if (writeChange) {
				RpcLogger.info("Initialize parameter, name - " + parameterName + ", value - " + newValue);
				rpcSettings.addParameter(parameterName, new RpcSettings.SettingsParameter(parameterName, fieldType, newValue));
				eventLoop.pushEvent(new OnSettingsParameterChangedEvent(alias, parameterName, fieldType, newValue));
			} else {
				throw new IllegalStateException("Trying to set parameter which does not exist, parameter name = " + parameterName);
			}
		} else {
			if (writeChange) {
				RpcLogger.info("New value will be saved for " + parameterName + ", value = " + newValue);
				rpcSettings.getParameter(parameterName).setValue(newValue);
				state.instrumentApi.setSettings(rpcSettings);
				parameterNameToPendingChanges.remove(parameterName);
				eventLoop.pushEvent(new OnSettingsParameterChangedEvent(alias, parameterName, fieldType, newValue));
			} else {
				RpcLogger.info("Pending changes for " + parameterName + ", value = " + newValue);
				if (parameter.getValue(Object.class).equals(newValue)) {
					parameterNameToPendingChanges.remove(parameterName);
				} else {
					parameterNameToPendingChanges.put(parameterName, new PendingParameterChanges(newValue, fieldType, parameterName, alias));
				}
			}
		}

		var isButtonEnabled = !parameterNameToPendingChanges.isEmpty();
		var applyButton = aliasToApplySettingsButton.get(alias);
		applyButton.setEnabled(isButtonEnabled);
	}

	private static GridBagConstraints getConstraintsForColors(int x, int y) {
		GridBagConstraints gbConst = new GridBagConstraints();
		setDefaultConstraints(gbConst, x, y);

		if (x == 1) {
			gbConst.anchor = GridBagConstraints.EAST;
		}

		gbConst.fill = GridBagConstraints.VERTICAL;
		return gbConst;
	}

	private static void setDefaultConstraints(GridBagConstraints gbConst, int x, int y) {
		gbConst.gridx = x;
		gbConst.gridy = y;
		gbConst.weightx = 1;
		gbConst.weighty = 1;
		gbConst.insets = new Insets(5, 5, 5, 5);
		gbConst.anchor = GridBagConstraints.WEST;
	}


	// TODO: this is trick by which number of lines is defined dynamically, try to find more consistent solution, Maybe move UI to external class
	// each line has exactly two components, label and components through which configuration can be made
	private int getNextRowForComponentInSettingPanel(State state) {
		return (state.settingsConfig.getComponentCount() - 1) / 2;
	}

	private int getNextRowForComponentInColorsPanel(State state) {
		return state.colorsConfig.getComponentCount() / 2;
	}


	private static GridBagConstraints getButtonConstraints() {
		GridBagConstraints gbConst = new GridBagConstraints();
		gbConst.gridx = 1;
		gbConst.weighty = 1;
		gbConst.insets = new Insets(5, 5, 5, 5);
		gbConst.anchor = GridBagConstraints.EAST;
		gbConst.weightx = 0.26;
		gbConst.fill = GridBagConstraints.NONE;
		gbConst.gridy = 4096 - 1;
		return gbConst;
	}

	private static class PendingParameterChanges {
		public final Object newValue;
		public final AddUiField.FieldType type;
		public final String name;
		public final String alias;

		private PendingParameterChanges(Object newValue, AddUiField.FieldType type, String name, String alias) {
			this.newValue = newValue;
			this.type = type;
			this.name = name;
			this.alias = alias;
		}
	}
}