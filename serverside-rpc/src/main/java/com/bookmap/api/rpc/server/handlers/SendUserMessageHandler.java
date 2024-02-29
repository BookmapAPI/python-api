package com.bookmap.api.rpc.server.handlers;

import com.bookmap.api.rpc.server.EventLoop;
import com.bookmap.api.rpc.server.State;
import com.bookmap.api.rpc.server.data.income.SendUserMessageEvent;
import velox.api.layer1.messages.Layer1ApiSoundAlertMessage;

import java.util.concurrent.ConcurrentMap;

public class SendUserMessageHandler implements Handler<SendUserMessageEvent> {
    private final EventLoop eventLoop;
    private final ConcurrentMap<String, State> aliasToState;

    public SendUserMessageHandler(EventLoop eventLoop, ConcurrentMap<String, State> aliasToState) {
        this.eventLoop = eventLoop;
        this.aliasToState = aliasToState;
    }

    @Override
    public void handle(SendUserMessageEvent event) {
        State state = aliasToState.get(event.alias);

        Layer1ApiSoundAlertMessage.Builder builder = Layer1ApiSoundAlertMessage.builder()
				.setShowPopup(true);
			builder.setTextInfo(event.message);
            builder.setSource(state.sourceClass);
			builder.setAlias(event.alias);

		state.instrumentApi.sendUserMessage(builder.build());
    }
}
