package com.bookmap.api.rpc.server.data.income;

import com.bookmap.api.rpc.server.data.utils.AbstractEventWithAlias;
import com.bookmap.api.rpc.server.data.utils.Type;

public class SubscribeToIndicatorEvent extends AbstractEventWithAlias {

    public final String addonName;
    public final boolean doesRequireFiltering;

    public SubscribeToIndicatorEvent(String addonName, String alias, boolean doesRequireFiltering) {
        super(Type.REGISTER_BROADCASTING_PROVIDER, alias);
        this.addonName = addonName;
        this.doesRequireFiltering = doesRequireFiltering;
    }
}
