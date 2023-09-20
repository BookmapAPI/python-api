package com.bookmap.api.rpc.server.handlers;

import com.bookmap.api.rpc.server.data.income.ClientInitEvent;
import com.bookmap.api.rpc.server.log.RpcLogger;

public class ClientInitHandler implements Handler<ClientInitEvent> {

	public ClientInitHandler() {}

	@Override
	public void handle(ClientInitEvent event) {
		RpcLogger.info("Client init received!");
	}
}
