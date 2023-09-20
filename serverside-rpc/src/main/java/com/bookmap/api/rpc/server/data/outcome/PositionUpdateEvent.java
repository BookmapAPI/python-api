package com.bookmap.api.rpc.server.data.outcome;

import com.bookmap.api.rpc.server.data.utils.AbstractEvent;
import com.bookmap.api.rpc.server.data.utils.Type;
import velox.api.layer1.data.StatusInfo;

public class PositionUpdateEvent extends AbstractEvent {
    public final StatusInfo statusInfo;
    public PositionUpdateEvent(StatusInfo statusInfo) {
        super(Type.POSITION_UPDATE);
        this.statusInfo = statusInfo;
    }
}
