package com.bookmap.api.rpc.server.handlers;

import com.bookmap.api.rpc.server.EventLoop;
import com.bookmap.api.rpc.server.addon.Connector;
import com.bookmap.api.rpc.server.data.income.SubscribeToGeneratorEvent;
import com.bookmap.api.rpc.server.log.RpcLogger;

import java.util.concurrent.*;

public class SubscribeToGeneratorHandler implements Handler<SubscribeToGeneratorEvent> {
    private final EventLoop eventLoop;
    private final Connector connector;
    private final ExecutorService service;

    public SubscribeToGeneratorHandler(EventLoop eventLoop, Connector connector, ExecutorService service) {
        this.eventLoop = eventLoop;
        this.connector = connector;
        this.service = service;
    }

    @Override
    public void handle(SubscribeToGeneratorEvent event) {
        if (event.generatorName == null) {
            RpcLogger.info("Generator name is null, connecting to provider " + event.addonName);
            Future<Boolean> isConnected = connector.connect(event.addonName);
            service.execute(() -> {
                try {
                    if (isConnected.get()) {
                        RpcLogger.info("Successfully connected to " + event.addonName);
                    }
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException(String.format(
                            "Exception during subscribing to live data for %s," +
                                    " indicator name: %s", event.generatorName, event.addonName), e);
                }
            });
        } else {
            if (connector.isConnectedToProvider(event.addonName)) {
                connector.subscribeToLiveData(event.generatorName, eventLoop, event.addonName, event.doesRequireFiltering);
                RpcLogger.info("Successfully connected to " + event.addonName + " " + event.generatorName);
                return;
            }
            Future<Boolean> isConnected = connector.connect(event.addonName);
            service.execute(() -> {
                try {
                    if (isConnected.get()) {
                        connector.subscribeToLiveData(event.generatorName, eventLoop, event.addonName, event.doesRequireFiltering);
                        RpcLogger.info("Successfully connected to " + event.addonName + " " + event.generatorName);
                    }
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException(String.format(
                            "Exception during subscribing to live data for %s," +
                                    " indicator name: %s", event.generatorName, event.addonName), e);
                }
            });
        }
    }
}
