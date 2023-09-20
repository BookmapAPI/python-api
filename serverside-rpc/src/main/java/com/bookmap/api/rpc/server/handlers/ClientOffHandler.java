package com.bookmap.api.rpc.server.handlers;

import com.bookmap.api.rpc.server.EventLoop;
import com.bookmap.api.rpc.server.data.income.ClientOffEvent;
import com.bookmap.api.rpc.server.data.outcome.ServerOffEvent;
import com.bookmap.api.rpc.server.log.RpcLogger;

public class ClientOffHandler implements Handler<ClientOffEvent> {

	private final EventLoop eventLoop;

	public ClientOffHandler(EventLoop eventLoop) {
		this.eventLoop = eventLoop;
	}

	@Override
	public void handle(ClientOffEvent event) {
		RpcLogger.info("Received event that client is off");
		eventLoop.pushEvent(new ServerOffEvent());
	}
}
