package com.bookmap.api.rpc.server.communication;

/**
 * Entity which handles reading and initial processing of messages delivered from {@link Server}. The entity of this
 * class is one time use, in other words after {@link this#stop()} is called, no guarantee that calling {@link this#start()}
 * will work as expected.
 */
public interface MessageReader {

	/**
	 * Initiate process of reading and handling of messages.
	 */
	void start();

	/**
	 * Stops reading events, however existing events can still might processed.
	 */
	void stop();
}
