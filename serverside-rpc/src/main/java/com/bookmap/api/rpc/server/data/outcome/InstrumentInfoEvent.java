package com.bookmap.api.rpc.server.data.outcome;

import com.bookmap.api.rpc.server.data.utils.AbstractEventWithAlias;
import com.bookmap.api.rpc.server.data.utils.Type;

public class InstrumentInfoEvent extends AbstractEventWithAlias {

	public final double pips;
	public final double sizeMultiplier;
	public final double instrumentMultiplier;
	public final boolean isCrypto;
	public final String fullName;

	public InstrumentInfoEvent(double pips, double sizeMultiplier, double instrumentMultiplier, boolean isCrypto, String fullName, String alias) {
		super(Type.INSTRUMENT_INFO, alias);
		this.pips = pips;
		this.sizeMultiplier = sizeMultiplier;
		this.instrumentMultiplier = instrumentMultiplier;
		this.isCrypto = isCrypto;
		this.fullName = fullName;
	}
}
