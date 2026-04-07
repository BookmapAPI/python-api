package com.bookmap.api.rpc.server.addon.listeners.broadcasting;

import com.bookmap.addons.broadcasting.api.view.GeneratorInfo;
import com.bookmap.addons.broadcasting.api.view.listeners.ProviderStatusListener;
import com.bookmap.api.rpc.server.services.ProviderStatusService;

import java.util.List;

public class RpcProviderStatusListener implements ProviderStatusListener {

    private final ProviderStatusService providerStatusService;

    public RpcProviderStatusListener(ProviderStatusService providerStatusService) {
        this.providerStatusService = providerStatusService;
    }

    @Override
    public void providerBecameAvailable(String providerName, String providerId) {
        providerStatusService.addProvider(providerName);
    }

    @Override
    public void providerBecameUnavailable(String providerName, String providerId) {
        providerStatusService.removeProvider(providerName);
    }

    @Override
    public void providerUpdateGenerator(String providerName, String providerId, GeneratorInfo generator, boolean isOnline) {
        providerStatusService.updateProvider(providerName, generator, isOnline);
    }
}
