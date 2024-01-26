package com.bookmap.api.rpc.server.data.utils;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum Type {
	// general events
	CLIENT_INIT(0),
	SERVER_INIT(1),
	INSTRUMENT_INFO(2),
	// data events
	REQ_DATA(3),
	RESP_DATA(4),
	TRADE(5),
	BAR(6),
	INSTRUMENT_DETACHED(7),
	DEPTH(8),
	// indicator events
	REGISTER_INDICATOR(9),
	INDICATOR_RESPONSE(10),
	ADD_POINT_TO_INDICATOR(11),
	ON_INTERVAL(12),
	INITIALIZATION_FINISHED(13),
	// Settings UI
	ADD_SETTING_FIELD(14),
	ON_SETTINGS_PARAMETER_CHANGED(15),
	SERVER_OFF(16),
	CLIENT_OFF(17),
	MBO(18),
	// Trading events
	SEND_ORDER(19),
	EXECUTE_ORDER(20),
	UPDATE_ORDER(21),
	ORDER_INFO(22),
	CANCEL_ORDER(23),
	MOVE_ORDER(24),
	MOVE_ORDER_TO_MARKET(25),
	RESIZE_ORDER(26),
	BALANCE_UPDATE(27),
	POSITION_UPDATE(28),
	BROADCASTING(29),
	REGISTER_BROADCASTING_PROVIDER(30),
	ERROR(-1),
	UNKNOWN(-2147483648);

	private static final Map<Integer, Type> VAL_TO_ENUM;

	static {
		VAL_TO_ENUM = Arrays.stream(Type.values())
				.map(v -> Map.entry(v.code, v))
				.collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));
	}

	public final int code;

	Type(int code) {
		this.code = code;
	}

	public static Type of(int type) {
		return VAL_TO_ENUM.getOrDefault(type, UNKNOWN);
	}

}
