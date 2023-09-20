package integrational.utils;

import velox.api.layer1.Layer1ApiProvider;
import velox.api.layer1.data.OrderSendParameters;
import velox.api.layer1.data.OrderUpdateParameters;
import velox.api.layer1.messages.indicators.Layer1ApiUserMessageModifyIndicator;
import velox.api.layer1.simplified.*;

import java.awt.*;
import java.awt.image.BufferedImage;

public class TestApiWrapper implements Api {
	@Override
	public Indicator registerIndicator(String s, Layer1ApiUserMessageModifyIndicator.GraphType graphType, double v, boolean b, boolean isModifiable) {

		return new Indicator() {
			@Override
			public void addPoint(double v) {

			}

			@Override
			public void addIcon(double v, BufferedImage bufferedImage, int i, int i1) {

			}

			@Override
			public void setColor(Color color) {

			}

			@Override
			public void setWidth(int i) {

			}

			@Override
			public void setLineStyle(LineStyle lineStyle) {

			}

			@Override
			public void setRenderPriority(int i) {

			}

			@Override
			public void setAxisRules(AxisRules axisRules) {

			}

			@Override
			public void setWidgetRules(WidgetRules widgetRules) {

			}
		};
	}

	@Override
	public IndicatorModifiable registerIndicatorModifiable(String alias, Layer1ApiUserMessageModifyIndicator.GraphType graphType, double v, boolean b, boolean b1) {
		return null;
	}

	@Override
	public void sendOrder(OrderSendParameters orderSendParameters) {

	}

	@Override
	public void updateOrder(OrderUpdateParameters orderUpdateParameters) {

	}

	@Override
	public <T> void setSettings(T t) {

	}

	@Override
	public <T> T getSettings(Class<? extends T> aClass) {
		return null;
	}

	@Override
	public void unload() {

	}

	@Override
	public void reload() {

	}

	@Override
	public void addTimeListeners(TimeListener timeListener) {

	}

	@Override
	public void addDepthDataListeners(DepthDataListener depthDataListener) {

	}

	@Override
	public void addMarketByOrderDepthDataListeners(MarketByOrderDepthDataListener marketByOrderDepthDataListener) {

	}

	@Override
	public void addSnapshotEndListeners(SnapshotEndListener snapshotEndListener) {

	}

	@Override
	public void addTradeDataListeners(TradeDataListener tradeDataListener) {

	}

	@Override
	public void addIntervalListeners(IntervalListener intervalListener) {

	}

	@Override
	public void addBarDataListeners(BarDataListener barDataListener) {

	}

	@Override
	public void addBboDataListeners(BboListener bboListener) {

	}

	@Override
	public void addOrdersListeners(OrdersListener ordersListener) {

	}

	@Override
	public void addStatusListeners(PositionListener positionListener) {

	}

	@Override
	public void addBalanceListeners(BalanceListener balanceListener) {

	}

	@Override
	public void addHistoricalModeListeners(HistoricalModeListener historicalModeListener) {

	}

	@Override
	public void addMultiInstrumentListeners(MultiInstrumentListener multiInstrumentListener) {

	}

	@Override
	public void sendUserMessage(Object o) {

	}

	@Override
	public Layer1ApiProvider getProvider() {
		return null;
	}
}
