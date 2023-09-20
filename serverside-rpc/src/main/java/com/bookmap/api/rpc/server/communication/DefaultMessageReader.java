package com.bookmap.api.rpc.server.communication;

import com.bookmap.api.rpc.server.EventLoop;
import com.bookmap.api.rpc.server.data.utils.AbstractEvent;
import com.bookmap.api.rpc.server.data.utils.IncomeConverterManager;
import com.bookmap.api.rpc.server.data.utils.exceptions.FailedToConvertException;
import com.bookmap.api.rpc.server.log.RpcLogger;
import velox.api.layer1.common.Log;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class DefaultMessageReader implements MessageReader {

	private final Server server;
	private final IncomeConverterManager incomeConverterManager;
	private final EventLoop eventLoop;
	private final AtomicBoolean isRun = new AtomicBoolean(true);
	private final BlockingQueue<String> events = new LinkedBlockingQueue<>();
	private final ExecutorService messageHandlerExecutor = Executors.newFixedThreadPool(2);

	public DefaultMessageReader(Server server, IncomeConverterManager incomeConverterManager, EventLoop eventLoop) {
		this.server = server;
		this.incomeConverterManager = incomeConverterManager;
		this.eventLoop = eventLoop;
	}

	@Override
	public void start() {
		messageHandlerExecutor.execute(() -> {
			try {
				while (isRun.get() || !events.isEmpty()) {
					String clientEvent = events.poll(10, TimeUnit.SECONDS);
					if (clientEvent == null) {
						continue;
					}
					AbstractEvent eventEntity = incomeConverterManager.convert(clientEvent);
					eventLoop.pushEvent(eventEntity);
				}
			} catch (FailedToConvertException ex) {

			} catch (InterruptedException e) {
				Log.warn("Reader message waiting interrupted", e);
			}
		});
		messageHandlerExecutor.execute(() -> {
			while (isRun.get()) {
				String msg = server.receive();
				if (msg == null) {
					RpcLogger.warn("Message is null, reader is stopped");
					stop();
					break;
				}
				events.add(msg);
			}
		});
	}

	@Override
	public void stop() {
		isRun.set(false);
		messageHandlerExecutor.shutdown();
	}
}
