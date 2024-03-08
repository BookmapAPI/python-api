package com.bookmap.api.rpc.server.addon.listeners.broadcasting;

import com.bookmap.addons.broadcasting.api.view.BroadcasterConsumer;
import com.bookmap.addons.broadcasting.api.view.GeneratorInfo;
import com.bookmap.addons.broadcasting.api.view.listeners.LiveConnectionStatusListener;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The listener that will be notified by BrAPI when the provider's live data subscription state changes.
 */
public class LiveConnectionListener implements LiveConnectionStatusListener {
    private boolean liveConnectionStatus = false;
    private GeneratorInfo generatorInfo;
    private BroadcasterConsumer consumer;

    public LiveConnectionListener(GeneratorInfo generatorInfo, BroadcasterConsumer consumer) {
        this.generatorInfo = generatorInfo;
        this.consumer = consumer;
    }

    @Override
    public void reactToStatusChanges(boolean status) {
        liveConnectionStatus = status;
        System.out.println("subscribed!!!!");
        System.out.println("generatorInfo: " + generatorInfo.getSettings() + " filter " + generatorInfo.getFilter());
        List<GeneratorInfo> generatorInfos = consumer.getGeneratorsInfo("com.bookmap.addons.marketpulse.app.MarketPulse");
        for (GeneratorInfo generatorInfo : generatorInfos) {
            System.out.println("generatorInfo: " + generatorInfo.getSettings() + " filter " + generatorInfo.getFilter());
        }
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(() -> {
            try {
                Thread.sleep(1000);
                System.out.println("generatorInfo after 1 sec: " + generatorInfo.getSettings() + " filter " + generatorInfo.getFilter());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    public boolean isConnect() {
        return liveConnectionStatus;
    }
}
