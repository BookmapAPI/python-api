package com.bookmap.api.rpc.server.data.income;

import com.bookmap.api.rpc.server.data.utils.AbstractEventWithAlias;
import com.bookmap.api.rpc.server.data.utils.Type;

public class SubscribeToIndicatorEvent extends AbstractEventWithAlias {

    public final String addonName;

    public SubscribeToIndicatorEvent(String addonName, String alias) {
        super(Type.REGISTER_BROADCASTING_PROVIDER, alias);
        this.addonName = addonName;
    }
}
