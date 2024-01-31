package com.bookmap.api.rpc.server.data.outcome;

import com.bookmap.api.rpc.server.data.utils.AbstractEvent;
import com.bookmap.api.rpc.server.data.utils.Type;

public class BroadcastingEvent extends AbstractEvent {
    public final String event;
    public final String generatorName;

    public BroadcastingEvent(String generatorName, String event) {
        super(Type.BROADCASTING);
        this.generatorName = generatorName;
        this.event = event;
    }
}
