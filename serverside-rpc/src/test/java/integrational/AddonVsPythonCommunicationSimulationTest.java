package integrational;

import com.bookmap.api.rpc.server.State;
import com.bookmap.api.rpc.server.addon.TestInstance;
import com.bookmap.api.rpc.server.addon.listeners.data.RpcDepthDataListener;
import com.bookmap.api.rpc.server.addon.listeners.data.RpcMboDataListener;
import com.bookmap.api.rpc.server.addon.listeners.trading.RpcTradeDataListener;
import com.bookmap.api.rpc.server.data.outcome.InstrumentDetachedEvent;
import com.bookmap.api.rpc.server.data.outcome.InstrumentInfoEvent;
import com.bookmap.api.rpc.server.data.utils.Type;
import integrational.utils.Channel;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;
import velox.api.layer1.data.InstrumentInfo;
import velox.api.layer1.simplified.Api;
import velox.api.layer1.simplified.Indicator;
import velox.api.layer1.simplified.IndicatorModifiable;
import velox.api.layer1.simplified.InitialState;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * This class emulates events from the client using file source
 */
public class AddonVsPythonCommunicationSimulationTest {

	private static final String OUTPUT_FILE = "output.txt";

	private TestInstance testInstance;
	private Channel destinationChannel;
	private File sourceFile;

	@BeforeEach
	public void runBeforeEachTest() throws IOException {
		this.destinationChannel = new Channel();
		Path outputPath = Path.of(OUTPUT_FILE);
		Files.deleteIfExists(outputPath);
		Files.createFile(outputPath);
	}

	@AfterEach
	public void runAfterEachTest() {
		testInstance.stop();
		this.destinationChannel.close();
	}

	@Test
	public void testInitialization() throws InterruptedException, TimeoutException {
		sourceFile = new File("integrational/client_init_only_event.txt");
		testInstance = new TestInstance(sourceFile, destinationChannel);

		testInstance.run();
		destinationChannel.waitForClose(2, TimeUnit.SECONDS);

		List<String> result = new ArrayList<>(destinationChannel);
		Assertions.assertEquals(1, result.size(), "Wrong output size");
		Assertions.assertEquals(String.valueOf(Type.SERVER_INIT.code), result.get(0));
	}

	@Test
	@Timeout(5)
	public void testInstrumentInfoDelivery() throws InterruptedException {
		sourceFile = new File("integrational/instrument_info_subscribe_detach_test.txt");
		testInstance = new TestInstance(sourceFile, destinationChannel);

		testInstance.run();

		String serverInitExpectedMsg = destinationChannel.take();
		Assertions.assertEquals(String.valueOf(Type.SERVER_INIT.code), serverInitExpectedMsg);

		var eventLoop = testInstance.getEventLoop();
		eventLoop.pushEvent(new InstrumentInfoEvent(0.01, 1, 100, false, "STOCK TEST INSTRUMENT", "AAPL@DXFEED", null));
		eventLoop.pushEvent(new InstrumentInfoEvent(0.00001d, 100d, 1d, true, "CRYPTO TEST INSTRUMENT", "BTC-USD@KRAKEN", null));
		eventLoop.pushEvent(new InstrumentInfoEvent(0.025d, 1d, 25d, false, "FUTURES TEST INSTRUMENT", "ESU25@RITHMIC", null));

		eventLoop.pushEvent(new InstrumentDetachedEvent("BTC-USD@KRAKEN"));
		eventLoop.pushEvent(new InstrumentDetachedEvent("ESU25@RITHMIC"));
		eventLoop.pushEvent(new InstrumentDetachedEvent("AAPL@DXFEED"));

		Assertions.assertEquals("2\uE000AAPL@DXFEED\uE000STOCK TEST INSTRUMENT\uE0000\uE0000.01\uE0001.0\uE000100.0", destinationChannel.take());
		Assertions.assertEquals("2\uE000BTC-USD@KRAKEN\uE000CRYPTO TEST INSTRUMENT\uE0001\uE0001.0E-5\uE000100.0\uE0001.0", destinationChannel.take());
		Assertions.assertEquals("2\uE000ESU25@RITHMIC\uE000FUTURES TEST INSTRUMENT\uE0000\uE0000.025\uE0001.0\uE00025.0", destinationChannel.take());
		Assertions.assertEquals("7\uE000BTC-USD@KRAKEN", destinationChannel.take());
		Assertions.assertEquals("7\uE000ESU25@RITHMIC", destinationChannel.take());
		Assertions.assertEquals("7\uE000AAPL@DXFEED", destinationChannel.take());
	}

	@Test
	@Timeout(10)
	public void testIndicatorCreationRequest() throws InterruptedException {
		sourceFile = new File("integrational/indicators_creation_events.txt");
		testInstance = new TestInstance(sourceFile, destinationChannel);
		Api mockedApi = mock(Api.class);
		Indicator mockedIndicator = mock(Indicator.class);
		IndicatorModifiable mockedModifiableIndicator = mock(IndicatorModifiable.class);

		when(mockedApi.registerIndicator(anyString(), any(), anyDouble(), anyBoolean(), anyBoolean()))
				.thenReturn(mockedIndicator);
		when(mockedApi.registerIndicatorModifiable(anyString(), any(), anyDouble(), anyBoolean(), anyBoolean()))
				.thenReturn(mockedModifiableIndicator);

		State state = new State(new InstrumentInfo("AAPL", null, null, 0.01, 0.01, "AAPL DXFEED", true), mockedApi, new InitialState());
		testInstance.aliasToState.put("AAPL@DXFEED", state);
		testInstance.run();


		String serverInitExpectedMsg = destinationChannel.take();
		Assertions.assertEquals(String.valueOf(Type.SERVER_INIT.code), serverInitExpectedMsg);

		Assertions.assertEquals("10\uE00011\uE00042", destinationChannel.take());
		Assertions.assertEquals("10\uE00012\uE00043", destinationChannel.take());
		Assertions.assertEquals("10\uE00013\uE00044", destinationChannel.take());
		Assertions.assertEquals("10\uE00014\uE00045", destinationChannel.take());
		Assertions.assertEquals("10\uE00015\uE00046", destinationChannel.take());
		Assertions.assertEquals("10\uE00016\uE00047", destinationChannel.take());
		Assertions.assertEquals("10\uE00017\uE00048", destinationChannel.take());
		Assertions.assertEquals("10\uE00018\uE00049", destinationChannel.take());
		Assertions.assertEquals("10\uE00019\uE00050", destinationChannel.take());
		Assertions.assertEquals("10\uE00020\uE00051", destinationChannel.take());
		Assertions.assertEquals("10\uE00021\uE00052", destinationChannel.take());
	}

	@Test
	@Timeout(10)
	public void testDataRequest() throws InterruptedException {
		sourceFile = new File("integrational/data_requests.txt");
		testInstance = new TestInstance(sourceFile, destinationChannel);
		Api btcMockedApi = mock(Api.class);
		Api ethMockedApi = mock(Api.class);
		Api ESMockedApi = mock(Api.class);

		State btcUsdtState = new State(new InstrumentInfo("BTC-USDT", null, null, 10, 0.01, "Bitcoin USDT", true), btcMockedApi, new InitialState());
		State ethUsdtState = new State(new InstrumentInfo("ETH-USDT", null, null, 1, 0.01, "Ethereum USDT", true), ethMockedApi, new InitialState());
		State ESMockedState = new State(new InstrumentInfo("ESU22", null, null, 0.25, 1, "S&P...", true), ESMockedApi, new InitialState());

		testInstance.aliasToState.put("BTC-USDT@KR", btcUsdtState);
		testInstance.aliasToState.put("ETH-USDT@BN", ethUsdtState);
		testInstance.aliasToState.put("ESU22@DXFEED", ESMockedState);
		testInstance.run();

		String serverInitExpectedMsg = destinationChannel.take();
		Assertions.assertEquals(String.valueOf(Type.SERVER_INIT.code), serverInitExpectedMsg);

		Assertions.assertEquals("4\uE00022", destinationChannel.take());
		Assertions.assertEquals("4\uE00023", destinationChannel.take());
		Assertions.assertEquals("4\uE00024", destinationChannel.take());

		ArgumentCaptor<RpcDepthDataListener> depthDataListenerArgument = ArgumentCaptor.forClass(RpcDepthDataListener.class);
		ArgumentCaptor<RpcTradeDataListener> tradeDataListenerArgument = ArgumentCaptor.forClass(RpcTradeDataListener.class);
		ArgumentCaptor<RpcMboDataListener> mboDataListenerArgument = ArgumentCaptor.forClass(RpcMboDataListener.class);

		// check that required API calls were triggered
		verify(btcMockedApi).addDepthDataListeners(depthDataListenerArgument.capture());
		verify(ethMockedApi).addTradeDataListeners(tradeDataListenerArgument.capture());
		verify(ESMockedApi).addMarketByOrderDepthDataListeners(mboDataListenerArgument.capture());
	}
}
