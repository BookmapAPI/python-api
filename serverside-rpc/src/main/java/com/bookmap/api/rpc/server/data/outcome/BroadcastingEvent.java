package com.bookmap.api.rpc.server.data.outcome;

import com.bookmap.api.rpc.server.data.utils.AbstractEventWithAlias;
import com.bookmap.api.rpc.server.data.utils.Type;

public class BroadcastingEvent extends AbstractEventWithAlias {
    public final String event;
    public BroadcastingEvent(Type type, String alias, String event) {
        super(type, alias);
        this.event = event;
    }
}
