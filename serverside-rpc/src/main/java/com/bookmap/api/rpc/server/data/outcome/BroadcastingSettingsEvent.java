package com.bookmap.api.rpc.server.data.outcome;

import com.bookmap.api.rpc.server.data.utils.AbstractEvent;
import com.bookmap.api.rpc.server.data.utils.Type;

public class BroadcastingSettingsEvent extends AbstractEvent {

    public final String event;
    public final String generatorName;

    public BroadcastingSettingsEvent(String generatorName, String event) {
        super(Type.BROADCASTING_SETTINGS);
        this.generatorName = generatorName;
        this.event = event;
    }
}
