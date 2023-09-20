package com.bookmap.api.rpc.server.addon;

import com.bookmap.api.rpc.server.EventLoop;

/**
 * Entity wrapper for the whole backend infrastructure. It runs and stops work of the server.
 */
public interface Instance {

	void run();

	void stop();

	// TODO: event loop often should be shared with classes not related to instance, however it is better to avoid this
	//  by improving initialization model, Dagger can help with it, still need just to figure out how
	EventLoop getEventLoop();

	boolean isRun();
}
