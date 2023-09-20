package com.bookmap.api.rpc.server.communication;

import com.bookmap.api.rpc.server.exceptions.FailedToStartServerException;
import integrational.utils.Channel;
import org.junit.jupiter.api.Assertions;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Mock server is responsible for emulating {@link Server} for testing purposes using text file as a source of events.
 * It is supposed to be used running integration tests without actual need to have a script of Python code
 */
public class MockServer implements Server {

	private final BufferedReader reader;
	private final Channel destination;
	private final AtomicBoolean isAlive = new AtomicBoolean(false);

	public MockServer(File eventsSource, Channel destination) throws IOException {
		this.reader = new BufferedReader(new FileReader(eventsSource, StandardCharsets.UTF_8));
		this.destination = destination;
		this.isAlive.set(true);
	}

	@Override
	public String receive() {
		try {
			return reader.readLine();
		} catch (IOException e) {
			Assertions.fail("Failed to read line", e);
			return null;
		}
	}

	@Override
	public void send(String msg) {
		try {
			destination.put(msg);
		} catch (Exception e) {
			Assertions.fail("Failed to put element to the result channel", e);
		}
	}

	@Override
	public void start() throws FailedToStartServerException {
		// once it is created, it is ready to be used
	}

	@Override
	public boolean isAlive() {
		return isAlive.get(); // always alive until created
	}

	@Override
	public void close() throws IOException {
		isAlive.set(false);
		reader.close();
		destination.close();
	}
}
