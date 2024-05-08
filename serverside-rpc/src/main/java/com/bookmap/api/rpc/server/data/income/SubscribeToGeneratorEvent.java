package com.bookmap.api.rpc.server.data.income;

import com.bookmap.api.rpc.server.data.utils.AbstractEvent;
import com.bookmap.api.rpc.server.data.utils.Type;

public class SubscribeToGeneratorEvent extends AbstractEvent {

    public final String addonName;
    public final boolean doesRequireFiltering;
    public final String generatorName;

    public SubscribeToGeneratorEvent(String addonName, String generatorName, boolean doesRequireFiltering) {
        super(Type.REGISTER_BROADCASTING_PROVIDER);
        this.generatorName = generatorName;
        this.addonName = addonName;
        this.doesRequireFiltering = doesRequireFiltering;
    }
}
