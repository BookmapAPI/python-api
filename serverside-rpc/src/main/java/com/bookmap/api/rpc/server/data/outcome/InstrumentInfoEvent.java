package com.bookmap.api.rpc.server.data.outcome;

import com.bookmap.api.rpc.server.data.utils.AbstractEventWithAlias;
import com.bookmap.api.rpc.server.data.utils.Type;
import velox.api.layer1.data.Layer1ApiProviderSupportedFeatures;

public class InstrumentInfoEvent extends AbstractEventWithAlias {

	public final double pips;
	public final double sizeMultiplier;
	public final double instrumentMultiplier;
	public final boolean isCrypto;
	public final String fullName;
	public final Layer1ApiProviderSupportedFeatures supportedFeatures;

	public InstrumentInfoEvent(double pips, double sizeMultiplier, double instrumentMultiplier, boolean isCrypto, String fullName, String alias, Layer1ApiProviderSupportedFeatures supportedFeatures) {
		super(Type.INSTRUMENT_INFO, alias);
		this.pips = pips;
		this.sizeMultiplier = sizeMultiplier;
		this.instrumentMultiplier = instrumentMultiplier;
		this.isCrypto = isCrypto;
		this.fullName = fullName;
		this.supportedFeatures = supportedFeatures;
	}

	@Override
	public String toString() {
		return "InstrumentInfoEvent{" +
				"pips=" + pips +
				", sizeMultiplier=" + sizeMultiplier +
				", instrumentMultiplier=" + instrumentMultiplier +
				", isCrypto=" + isCrypto +
				", fullName='" + fullName + '\'' +
				", supportedFeatures=" + supportedFeatures +
				", type=" + type +
				", alias='" + alias + '\'' +
				'}';
	}
}
