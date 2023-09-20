package com.bookmap.api.rpc.server.data.income;

import com.bookmap.api.rpc.server.data.utils.AbstractEventWithAlias;
import com.bookmap.api.rpc.server.data.utils.Type;
import velox.api.layer1.data.OrderUpdateParameters;

public class UpdateOrderEvent extends AbstractEventWithAlias {

    public final OrderUpdateParameters updateParameters;

    public UpdateOrderEvent(Type type, String alias, OrderUpdateParameters updateParameters) {
        super(type, alias);
        this.updateParameters = updateParameters;
    }
}
