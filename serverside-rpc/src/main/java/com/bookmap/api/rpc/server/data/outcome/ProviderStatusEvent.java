package com.bookmap.api.rpc.server.data.outcome;

import com.bookmap.api.rpc.server.data.utils.AbstractEvent;
import com.bookmap.api.rpc.server.data.utils.Type;

import java.util.List;
import java.util.Map;

public class ProviderStatusEvent extends AbstractEvent {
    public final Map<String, List<String>> availableProvidersToGenerators;

    public ProviderStatusEvent(Map<String, List<String>> availableProvidersToGenerators) {
        super(Type.PROVIDERS_STATUS);
        this.availableProvidersToGenerators = availableProvidersToGenerators;
    }
}

