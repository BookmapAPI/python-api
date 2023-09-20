package com.bookmap.api.rpc.server.data.converters;

import com.bookmap.api.rpc.server.data.outcome.MboDataEvent;
import com.bookmap.api.rpc.server.data.outcome.converters.MboDataEventConverter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MboDataEventConverterTest {

	private final MboDataEventConverter converter = new MboDataEventConverter();

	@Test
	public void testAskNewMboEvent() {
		MboDataEvent mboDataEvent = new MboDataEvent("BTC-USDT", "ORDER_NUMBER_1", MboDataEvent.MboDataType.ASK_NEW, 200, 300);
		String event = converter.convert(mboDataEvent);

		Assertions.assertEquals("18\uE000BTC-USDT\uE000ASK_NEW\uE000ORDER_NUMBER_1\uE000200\uE000300", event);
	}

	@Test
	public void testBidNewMboEvent() {
		MboDataEvent mboDataEvent = new MboDataEvent("BTC-USDT", "222331231", MboDataEvent.MboDataType.BID_NEW, 11, 2);
		String event = converter.convert(mboDataEvent);

		Assertions.assertEquals("18\uE000BTC-USDT\uE000BID_NEW\uE000222331231\uE00011\uE0002", event);
	}

	@Test
	public void testReplaceMboEvent() {
		MboDataEvent mboDataEvent = new MboDataEvent("BTC-USDT", "ooo22233311", MboDataEvent.MboDataType.REPLACE, 33, 1);
		String event = converter.convert(mboDataEvent);

		Assertions.assertEquals("18\uE000BTC-USDT\uE000REPLACE\uE000ooo22233311\uE00033\uE0001", event);
	}

	@Test
	public void testCancelMboEvent() {
		MboDataEvent mboDataEvent = new MboDataEvent("BTC-USDT", "999", MboDataEvent.MboDataType.CANCEL);
		String event = converter.convert(mboDataEvent);

		Assertions.assertEquals("18\uE000BTC-USDT\uE000CANCEL\uE000999\uE000-1\uE000-1", event);
	}
}
