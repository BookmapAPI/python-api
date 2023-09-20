package com.bookmap.api.rpc.server.communication;

import com.bookmap.api.rpc.server.exceptions.FailedToStartServerException;

import java.io.Closeable;

/**
 * Interface used for a low level communication between client and server.
 */
public interface Server extends Closeable {

	/**
	 * Reads event from the client or blocked until client sends something/connects
	 * to the server.
	 *
	 * @return message from the client
	 */
	String receive();

	/**
	 * Sends msg to client. Should not be called until {@link this#start()} is called and finishes.
	 *
	 * @param msg msg that user sends
	 */
	void send(String msg);

	/**
	 * Runs server and waits for a first and only client connection. Returns after user
	 * is connected. It is assumed that object is called only once in the beginning of
	 * a communication.
	 */
	void start() throws FailedToStartServerException;

	/**
	 * Checks whether client is connected to the server and server is alive
	 */
	boolean isAlive();
}
