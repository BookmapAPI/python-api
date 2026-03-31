package com.bookmap.api.rpc.server;

import com.bookmap.api.rpc.server.data.utils.AbstractEvent;
import com.bookmap.api.rpc.server.handlers.HandlerManager;
import com.bookmap.api.rpc.server.log.RpcLogger;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class EventLoop implements Closeable {

	private volatile HandlerManager handlerManager;
	private final BlockingQueue<AbstractEvent> events = new LinkedBlockingQueue<>();
	private final AtomicBoolean isRun = new AtomicBoolean(true);
	private final ExecutorService eventQueueReader = Executors.newSingleThreadExecutor();

	public EventLoop() {
		eventQueueReader.execute(() -> {
			try {
				while (isRun.get()) {
					if(this.handlerManager == null) {
						continue;
					}
					AbstractEvent event = events.poll(10, TimeUnit.SECONDS);
					if (event == null) {
						continue;
					}
					this.handlerManager.handle(event);
				}
				RpcLogger.info("Event handler thread stopped...");
			} catch (InterruptedException ex) {
				RpcLogger.warn("Interrupted event loop thread", ex);
			}
		});

	}

	public void pushEvent(AbstractEvent event) {
		events.add(event);
	}

	@Override
	public void close() throws IOException {
		isRun.set(false);
		eventQueueReader.shutdown();
	}

	public void setHandlerManager(HandlerManager handlerManager) {
		this.handlerManager = handlerManager;
	}
}
