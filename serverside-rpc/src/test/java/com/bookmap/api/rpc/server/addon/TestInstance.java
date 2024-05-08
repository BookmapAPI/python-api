package com.bookmap.api.rpc.server.addon;

import com.bookmap.api.rpc.server.EventLoop;
import com.bookmap.api.rpc.server.State;
import com.bookmap.api.rpc.server.communication.DefaultMessageReader;
import com.bookmap.api.rpc.server.communication.MessageReader;
import com.bookmap.api.rpc.server.communication.MockServer;
import com.bookmap.api.rpc.server.communication.Server;
import com.bookmap.api.rpc.server.data.outcome.ServerInitEvent;
import com.bookmap.api.rpc.server.data.utils.IncomeConverterManager;
import com.bookmap.api.rpc.server.data.utils.OutcomeConverterManager;
import com.bookmap.api.rpc.server.data.utils.modules.DaggerIncomeConverterManagerFactory;
import com.bookmap.api.rpc.server.data.utils.modules.DaggerOutcomeConverterManagerFactory;
import com.bookmap.api.rpc.server.exceptions.FailedToStartServerException;
import com.bookmap.api.rpc.server.exceptions.FatalServerException;
import com.bookmap.api.rpc.server.handlers.*;
import com.bookmap.api.rpc.server.handlers.indicators.AddPointIndicatorHandler;
import com.bookmap.api.rpc.server.handlers.indicators.RegisterIndicatorHandler;
import com.bookmap.api.rpc.server.log.RpcLogger;
import integrational.utils.Channel;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class TestInstance implements Instance {

	private final File clientEventsSourceFile;
	private final Channel destinationChannel;
	public final ConcurrentMap<String, State> aliasToState = new ConcurrentHashMap<>();
	private final ConcurrentMap<String, CompletableFuture<?>> aliasToInitializationTask = new ConcurrentHashMap<>();
	public Server server;
	private EventLoop eventLoop;
	private MessageReader reader;

	public TestInstance(File clientEventsSourceFile, Channel destinationChannel) {
		this.clientEventsSourceFile = clientEventsSourceFile;
		this.destinationChannel = destinationChannel;
	}

	@Override
	public void run() {
		try {
			server = new MockServer(clientEventsSourceFile, destinationChannel);
			server.start();
			eventLoop = new EventLoop();
			IncomeConverterManager incomeConverterManager = DaggerIncomeConverterManagerFactory.create().incomeConverterManager();
			OutcomeConverterManager outcomeConverterManager = DaggerOutcomeConverterManagerFactory.create().outcomeConverterManager();
			SendingEventToClientHandler sendingEventToClientHandler = new SendingEventToClientHandler(outcomeConverterManager, server);
			ReqDataHandler reqDataHandler = new ReqDataHandler(eventLoop, aliasToState);
			ClientInitHandler clientInitHandler = new ClientInitHandler();
			HandlerManager handlerManager = new HandlerManager(
					sendingEventToClientHandler, reqDataHandler, clientInitHandler,
					new RegisterIndicatorHandler(aliasToState, eventLoop), new AddPointIndicatorHandler(aliasToState, eventLoop), new FinishedInitializationHandler(aliasToInitializationTask),
					new ClientOffHandler(eventLoop), new AddUiFieldHandler(aliasToState, eventLoop),
					new SendOrderHandler(aliasToState, eventLoop), new UpdateOrderHandler(aliasToState, eventLoop), new SubscribeToGeneratorHandler(eventLoop, null, null),
					new SendUserMessageHandler(eventLoop, aliasToState));
			eventLoop.setHandlerManager(handlerManager);
			eventLoop.pushEvent(new ServerInitEvent());
			reader = new DefaultMessageReader(server, incomeConverterManager, eventLoop);
			reader.start();
			RpcLogger.info("Instance started");
		} catch (IOException | FailedToStartServerException e) {
			RpcLogger.error("Failed to run instance", e);
			throw new FatalServerException(e);
		}
	}

	@Override
	public void stop() {
		try {
			if (reader != null) {
				reader.stop();
			}
			if (eventLoop != null) {
				eventLoop.close();
				eventLoop = null;
			}
			server.close();
		} catch (IOException e) {
			RpcLogger.error("Failed to stop instance", e);
			throw new FatalServerException(e);
		}
	}

	@Override
	public EventLoop getEventLoop() {
		return eventLoop;
	}

	@Override
	public boolean isRun() {
		return false;
	}
}
