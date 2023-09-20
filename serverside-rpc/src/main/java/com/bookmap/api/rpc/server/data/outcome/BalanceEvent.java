package com.bookmap.api.rpc.server.data.outcome;

import com.bookmap.api.rpc.server.data.utils.AbstractEvent;
import com.bookmap.api.rpc.server.data.utils.Type;
import velox.api.layer1.data.BalanceInfo;

public class BalanceEvent extends AbstractEvent {
    public final BalanceInfo balanceInfo;
    public BalanceEvent(BalanceInfo balanceInfo) {
        super(Type.BALANCE_UPDATE);
        this.balanceInfo = balanceInfo;
    }
}
