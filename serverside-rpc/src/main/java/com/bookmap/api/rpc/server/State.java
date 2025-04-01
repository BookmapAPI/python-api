package com.bookmap.api.rpc.server;

import com.bookmap.api.rpc.server.addon.RpcSettings;
import velox.api.layer1.data.InstrumentInfo;
import velox.api.layer1.simplified.Api;
import velox.api.layer1.simplified.Indicator;
import velox.api.layer1.simplified.InitialState;
import velox.gui.StrategyPanel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class State {

	public final InstrumentInfo instrumentInfo;
	public final Api instrumentApi;
	public final InitialState instrumentInitialState;
	public volatile StrategyPanel colorsConfig;
	public volatile StrategyPanel settingsConfig;
	public final RpcSettings settings;
	public final Class sourceClass;

	public final Map<Integer, Indicator> aliasToIndicatorsAndTheirId = new ConcurrentHashMap<>();

	public State(InstrumentInfo instrumentInfo, Api instrumentApi, InitialState instrumentInitialState, Class sourceClass) {
		this.instrumentInfo = instrumentInfo;
		this.instrumentApi = instrumentApi;
		this.instrumentInitialState = instrumentInitialState;
		settings = instrumentApi.getSettings(RpcSettings.class);
        this.sourceClass = sourceClass;
    }

	public State(InstrumentInfo instrumentInfo, Api instrumentApi, InitialState instrumentInitialState) {
		this(instrumentInfo, instrumentApi, instrumentInitialState, null);
	}
}