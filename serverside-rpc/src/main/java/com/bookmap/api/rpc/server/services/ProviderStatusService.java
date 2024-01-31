package com.bookmap.api.rpc.server.services;

import com.bookmap.addons.broadcasting.api.view.BroadcasterConsumer;
import com.bookmap.addons.broadcasting.api.view.GeneratorInfo;
import com.bookmap.api.rpc.server.EventLoop;
import com.bookmap.api.rpc.server.data.outcome.ProviderStatusEvent;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ProviderStatusService {
    private final Map<String, List<String>> providerToGenerators = new ConcurrentHashMap<>();
    private final EventLoop eventLoop;
    private BroadcasterConsumer broadcaster;

    public ProviderStatusService(EventLoop eventLoop) {
        this.eventLoop = eventLoop;
    }

    public void addProvider(String providerName) {
        List<String> generatorNames = List.of();
        if (isProviderConnected(providerName)) {
            List<GeneratorInfo> generators = broadcaster.getGeneratorsInfo(providerName);
            generatorNames = generators.stream().map(GeneratorInfo::getGeneratorName).toList();
        }
        providerToGenerators.put(providerName, generatorNames);
        eventLoop.pushEvent(new ProviderStatusEvent(providerToGenerators));
    }

    public void removeProvider(String providerName) {
        providerToGenerators.remove(providerName);
        eventLoop.pushEvent(new ProviderStatusEvent(providerToGenerators));
    }

    public void updateProvider(String providerName, List<GeneratorInfo> generators) {
        providerToGenerators.put(providerName, generators.stream().map(GeneratorInfo::getGeneratorName).toList());
        eventLoop.pushEvent(new ProviderStatusEvent(providerToGenerators));
    }

    public void setBroadcaster(BroadcasterConsumer broadcaster) {
        this.broadcaster = broadcaster;
    }

    private boolean isProviderConnected(String providerName) {
        return broadcaster.getSubscriptionProviders().contains(providerName);
    }
}
