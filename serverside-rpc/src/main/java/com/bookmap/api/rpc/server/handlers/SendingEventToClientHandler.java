package com.bookmap.api.rpc.server.handlers;

import com.bookmap.api.rpc.server.communication.Server;
import com.bookmap.api.rpc.server.data.utils.AbstractEvent;
import com.bookmap.api.rpc.server.data.outcome.ErrorEvent;
import com.bookmap.api.rpc.server.data.utils.OutcomeConverterManager;
import com.bookmap.api.rpc.server.data.utils.exceptions.ErrorEventException;
import com.bookmap.api.rpc.server.exceptions.FatalServerException;
import com.bookmap.api.rpc.server.log.RpcLogger;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Handler responsible for sending event to the client. Every event is sent to client in a single
 * thread.
 */
@Singleton
public class SendingEventToClientHandler implements Handler<AbstractEvent> {

	private final OutcomeConverterManager outcomeConverterManager;
	private final Server server;
	private final ExecutorService executorService = Executors.newSingleThreadExecutor();

	@Inject
	public SendingEventToClientHandler(OutcomeConverterManager outcomeConverterManager, Server server) {
		this.outcomeConverterManager = outcomeConverterManager;
		this.server = server;
	}

	@Override
	public void handle(AbstractEvent event) {
		// it is important to send all messages to user via single thread
		executorService.execute(() -> {
			// TODO: This is temporary solution. Ideally we should implement showing our own popup instead of
			//  crashing Bookmap or add error handler in Python
			if (event instanceof ErrorEvent) {
				RpcLogger.error("Received error event: " + event);
				throw new ErrorEventException(((ErrorEvent) event).errorString);
			}
			String eventStr = outcomeConverterManager.convert(event);
			try {
				server.send(eventStr);
			} catch (FatalServerException exception) {
				RpcLogger.error("Can't send msg to client, if this is done after process is killed, then it is ok", exception);
			}
		});
	}
}
