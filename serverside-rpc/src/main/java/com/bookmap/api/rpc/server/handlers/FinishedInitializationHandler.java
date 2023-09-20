package com.bookmap.api.rpc.server.handlers;

import com.bookmap.api.rpc.server.data.income.InitializationFinishedEvent;
import com.bookmap.api.rpc.server.log.RpcLogger;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class FinishedInitializationHandler implements Handler<InitializationFinishedEvent> {

	public final Map<String, CompletableFuture<?>> aliasToFutureWaitingForInitialization;

	public FinishedInitializationHandler(Map<String, CompletableFuture<?>> aliasToFutureWaitingForInitialization) {
		this.aliasToFutureWaitingForInitialization = aliasToFutureWaitingForInitialization;
	}

	@Override
	public void handle(InitializationFinishedEvent event) {
		RpcLogger.info("Received finish initialization message for " + event.alias);
		CompletableFuture<?> future = aliasToFutureWaitingForInitialization.get(event.alias);
		if (future != null) {
			future.complete(null);
		}
	}
}
