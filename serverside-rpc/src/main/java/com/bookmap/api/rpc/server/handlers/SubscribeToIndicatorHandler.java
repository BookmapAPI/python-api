package com.bookmap.api.rpc.server.handlers;

import com.bookmap.api.rpc.server.EventLoop;
import com.bookmap.api.rpc.server.addon.Connector;
import com.bookmap.api.rpc.server.data.income.SubscribeToIndicatorEvent;
import com.bookmap.api.rpc.server.log.RpcLogger;

import java.util.concurrent.*;

public class SubscribeToIndicatorHandler implements Handler<SubscribeToIndicatorEvent> {
    private final EventLoop eventLoop;
    private final Connector connector;
    private final ExecutorService service;

    public SubscribeToIndicatorHandler(EventLoop eventLoop, Connector connector, ExecutorService service) {
        this.eventLoop = eventLoop;
        this.connector = connector;
        this.service = service;
    }

    @Override
    public void handle(SubscribeToIndicatorEvent event) {
        Future<Boolean> isConnected = connector.connect(event.addonName);
        service.execute(() -> {
            try {
                if (isConnected.get()) {
                    connector.subscribeToLiveData(event.alias, eventLoop, event.addonName, event.doesRequireFiltering);
                    RpcLogger.info("Successfully connected to " + event.addonName + " " + event.alias);
                }
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(String.format(
                        "Exception during subscribing to live data for %s," +
                        " indicator name: %s", event.alias, event.addonName), e);
            }
        });
    }
}
