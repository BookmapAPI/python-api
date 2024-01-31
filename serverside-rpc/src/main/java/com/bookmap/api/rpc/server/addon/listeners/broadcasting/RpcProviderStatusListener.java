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
    public void providerBecameAvailable(String providerName) {
        providerStatusService.addProvider(providerName);
    }

    @Override
    public void providerBecameUnavailable(String providerName) {
        providerStatusService.removeProvider(providerName);
    }

    @Override
    public void providerUpdateGenerators(String providerName, List<GeneratorInfo> generators) {
        providerStatusService.updateProvider(providerName, generators);
    }
}
