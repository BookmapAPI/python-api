package com.bookmap.api.rpc.server.data.income;

import com.bookmap.api.rpc.server.data.utils.AbstractEventWithAlias;
import velox.api.layer1.messages.indicators.Layer1ApiUserMessageModifyIndicator;
import velox.api.layer1.simplified.LineStyle;

import java.awt.*;

import static com.bookmap.api.rpc.server.data.utils.Type.REGISTER_INDICATOR;

//TODO: add AxisRules, WidgetRules and rest support as well as changing of a parameters in runtime
public class RegisterIndicatorEvent extends AbstractEventWithAlias {

	public final long requestId;
	public final String name;
	public final Layer1ApiUserMessageModifyIndicator.GraphType graphType;
	public final double initialValue;
	public final boolean showLineByDefault;
	public final boolean showWidgetByDefault;
	public final boolean isModifiable;
	public final Color color;
	public final LineStyle lineStyle;


	public RegisterIndicatorEvent(String alias, long requestId, String name, Layer1ApiUserMessageModifyIndicator.GraphType graphType, double initialValue, boolean showLineByDefault, boolean showWidgetByDefault, boolean isModifiable, Color color, LineStyle lineStyle) {
		super(REGISTER_INDICATOR, alias);
		this.requestId = requestId;
		this.name = name;
		this.graphType = graphType;
		this.initialValue = initialValue;
		this.showLineByDefault = showLineByDefault;
		this.showWidgetByDefault = showWidgetByDefault;
		this.isModifiable = isModifiable;
		this.color = color;
		this.lineStyle = lineStyle;
	}
}
