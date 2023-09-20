package com.bookmap.api.rpc.server.data.converters;

import com.bookmap.api.rpc.server.data.income.ReqDataEvent;
import com.bookmap.api.rpc.server.data.utils.Type;
import com.bookmap.api.rpc.server.data.income.converters.ReqDataConverter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ReqDataEventConverterTest {

	private final ReqDataConverter converter = new ReqDataConverter();

	@Test
	public void testMboDataReq() {
		String mboSendAskEvent = "3\uE000BTC-USDT\uE00022\uE00018";
		ReqDataEvent mboReqDataEvent = converter.convert(mboSendAskEvent);

		Assertions.assertEquals(Type.REQ_DATA, mboReqDataEvent.type);
		Assertions.assertEquals(Type.MBO, mboReqDataEvent.typeOfRequestedData);
		Assertions.assertEquals(22, mboReqDataEvent.requestId);
		Assertions.assertEquals("BTC-USDT", mboReqDataEvent.alias);
	}
}
