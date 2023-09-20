package com.bookmap.api.rpc.server.communication;

import com.bookmap.api.rpc.server.exceptions.FailedToStartServerException;
import com.bookmap.api.rpc.server.exceptions.FatalServerException;

import java.io.*;

/**
 * Handles communication in case when client is run by an addon.
 */
public class InterProcessServer implements Server {

	private final Process process;
	private final BufferedReader reader;
	private final PrintWriter writer;

	public InterProcessServer(Process process) {
		this.process = process;
		this.reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
		this.writer = new PrintWriter(new OutputStreamWriter(process.getOutputStream()), true);
	}

	@Override
	public String receive() {
		try {
			return reader.readLine();
		} catch (Exception ex) {
			throw new FatalServerException(ex);
		}
	}

	@Override
	public void send(String msg) {
		writer.println(msg);
		if (writer.checkError()) {
			throw new FatalServerException("Failed to send msg to the process");
		}
	}

	@Override
	public void start() throws FailedToStartServerException {
		if (!process.isAlive()) {
			throw new FailedToStartServerException("Can't start " + this.getClass().getName() + ". Process is not alive");
		}
	}

	@Override
	public boolean isAlive() {
		return process.isAlive();
	}

	@Override
	public void close() throws IOException {
		process.destroy();
		reader.close();
		writer.close();
	}
}
