package com.bookmap.api.rpc.server.data.outcome;

import com.bookmap.api.rpc.server.data.utils.AbstractEventWithAlias;
import com.bookmap.api.rpc.server.data.utils.Type;
import velox.api.layer1.data.ExecutionInfo;

public class OrderExecutionEvent extends AbstractEventWithAlias {
    public final ExecutionInfo executionInfo;

    public OrderExecutionEvent(String alias, ExecutionInfo executionInfo) {
        super(Type.EXECUTE_ORDER, alias);
        this.executionInfo = executionInfo;
    }
}
