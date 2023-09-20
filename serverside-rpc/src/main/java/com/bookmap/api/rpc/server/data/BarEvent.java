package com.bookmap.api.rpc.server.data;

import com.bookmap.api.rpc.server.data.utils.AbstractEventWithAlias;
import com.bookmap.api.rpc.server.data.utils.Type;

public class BarEvent extends AbstractEventWithAlias {

	public final double open;
	public final double close;
	public final double high;
	public final double low;
	public final long volumeBuy;
	public final long volumeSell;
	public final double volumeTotal;
	public final double vwap;
	public final double vwapSell;
	public final double vwapBuy;

	public BarEvent(String alias, double open, double close, double high, double low, long volumeBuy, long volumeSell, double volumeTotal, double vwap, double vwapSell, double vwapBuy) {
		super(Type.BAR, alias);
		this.open = open;
		this.close = close;
		this.high = high;
		this.low = low;
		this.volumeBuy = volumeBuy;
		this.volumeSell = volumeSell;
		this.volumeTotal = volumeTotal;
		this.vwap = vwap;
		this.vwapSell = vwapSell;
		this.vwapBuy = vwapBuy;
	}
}
