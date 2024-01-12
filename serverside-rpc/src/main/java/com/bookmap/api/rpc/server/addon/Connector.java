package com.bookmap.api.rpc.server.addon;

import com.bookmap.addons.broadcasting.api.view.BroadcasterConsumer;
import com.bookmap.addons.broadcasting.api.view.Event;
import com.bookmap.addons.broadcasting.api.view.GeneratorInfo;

import com.bookmap.api.rpc.server.EventLoop;
import com.bookmap.api.rpc.server.addon.listeners.ConnectionListener;
import com.bookmap.api.rpc.server.addon.listeners.EventListener;
import com.bookmap.api.rpc.server.addon.listeners.LiveConnectionListener;
import com.bookmap.api.rpc.server.log.RpcLogger;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.*;

public class Connector {
    private final BroadcasterConsumer broadcasterConsumer;
    private final ConnectionListener connectionListener;
    private final Map<String, LiveConnectionListener> liveSubscriptionListenersByAlias = new ConcurrentHashMap<>();
    private final ExecutorService service = Executors.newSingleThreadExecutor();

    public Connector(BroadcasterConsumer broadcasterConsumer) {
        this.broadcasterConsumer = broadcasterConsumer;
        connectionListener = new ConnectionListener();
    }


    public Future<Boolean> connect(String providerName) {
        RpcLogger.info("Connecting to " + providerName);
        broadcasterConsumer.connectToProvider(providerName, connectionListener);
        return service.submit(() -> {
            int numberOfTries = 30;
            for (int i = 1; i <= numberOfTries; i++) {
                if (isConnected()) {
                    RpcLogger.info("Successfully connected on " + i + " try");
                    return true;
                }
                try {
                    if (i % 10 == 0) {
                        RpcLogger.info(String.format("Tried to connect %d times", i));
                    }
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    RpcLogger.error("Subscribing to live data interrupted ", e);
                }
            }
            return false;
        });
    }

    public boolean isConnected(){
        return connectionListener.isConnected();
    }


    public void subscribeToLiveData(String alias, EventLoop eventLoop, String providerName) {
        if (isConnected()) {
            Optional<GeneratorInfo> generatorInfoOptional = getGeneratorInfo(alias, providerName);
            generatorInfoOptional.ifPresent(generatorInfo -> {
                LiveConnectionListener subscriptionListener =
                        liveSubscriptionListenersByAlias.computeIfAbsent(alias, listener ->
                                new LiveConnectionListener());

                // Creating a listener for the events themselves.
                EventListener eventListener = new EventListener(eventLoop, alias);

                // Trying to subscribe for live events.
                // Broadcasting will notify us of a successful subscription through the LiveConnectionListener.
                broadcasterConsumer.subscribeToLiveData(providerName, generatorInfo.getGeneratorName(),
                        Event.class, eventListener, subscriptionListener);
                broadcasterConsumer.setListenersForGenerator(providerName, alias, null, null);
            });
        }
    }

    private Optional<GeneratorInfo> getGeneratorInfo(String alias, String providerName) {
        GeneratorInfo generatorInfo = null;
        List<GeneratorInfo> generatorsInfo = broadcasterConsumer.getGeneratorsInfo(providerName);
        for (GeneratorInfo generator : generatorsInfo) {
            // In AbsorptionIndicator, generators have the names of the aliases they work with.
            // Therefore, according to the alias, we will get a suitable generator for us.
            if (generator.getGeneratorName().equals(alias)) {
                generatorInfo = generator;
                break;
            }
        }
        return Optional.ofNullable(generatorInfo);
    }

    public void finish() {
        service.shutdownNow();
    }

    public boolean isSubscribeToLive(String alias){
        LiveConnectionListener listener = liveSubscriptionListenersByAlias.get(alias);
        return listener != null && listener.isConnect();
    }
}
