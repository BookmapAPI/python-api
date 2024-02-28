package com.bookmap.api.rpc.server.data.income;

import com.bookmap.api.rpc.server.data.utils.AbstractEventWithAlias;
import com.bookmap.api.rpc.server.data.utils.Type;

public class SendUserMessageEvent extends AbstractEventWithAlias {

    public final String message;
    public SendUserMessageEvent(String alias, String message) {
        super(Type.SEND_USER_MESSAGE, alias);
        this.message = message;
    }
}
