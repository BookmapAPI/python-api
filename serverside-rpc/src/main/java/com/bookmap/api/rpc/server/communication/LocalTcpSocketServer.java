package com.bookmap.api.rpc.server.communication;

import com.bookmap.api.rpc.server.exceptions.FailedToStartServerException;
import com.bookmap.api.rpc.server.exceptions.FatalServerException;
import com.bookmap.api.rpc.server.log.RpcLogger;
import velox.api.layer1.common.Log;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

// TODO: merge with unix socket approach
public class LocalTcpSocketServer implements Server {

	private static final int TIME_OUT_IN_MILLISECONDS = 3000;

	private final ServerSocket serverSocket;
	private Socket clientSocket;
	private BufferedReader clientReader;
	private PrintWriter clientWriter;

	public LocalTcpSocketServer(int port) throws IOException {
		this.serverSocket = new ServerSocket(port);
		this.serverSocket.setSoTimeout(TIME_OUT_IN_MILLISECONDS);
	}

	@Override
	public String receive() {
		try {
			String msg = clientReader.readLine();
			RpcLogger.debug("Received msg: " + msg);
			return msg;
		} catch (IOException e) {
			Log.warn("Failed to read message", e);
			return null;
		}
	}

	@Override
	public void send(String msg) {
		clientWriter.println(msg);
		if (clientWriter.checkError()) {
			throw new FatalServerException("Failed to send msg to the process; msg: " + msg);
		}
		RpcLogger.debug("Sent msg: " + msg);
	}

	@Override
	public void start() throws FailedToStartServerException {
		while (true) { // try to get client connection periodically until server is closed due to healthcheck or client is connected
			try {
				this.clientSocket = serverSocket.accept();
				this.clientReader = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream(), StandardCharsets.UTF_8));
				this.clientWriter = new PrintWriter(new OutputStreamWriter(this.clientSocket.getOutputStream(), StandardCharsets.UTF_8), true);
				break;
			} catch (IOException e) {
				if (serverSocket.isClosed()) {
					throw new FailedToStartServerException(e);
				}
				RpcLogger.error("Failed to accept client socket", e);
			}
		}
	}

	@Override
	public boolean isAlive() {
		return this.clientSocket == null || (this.clientSocket.isConnected() && !this.clientSocket.isClosed());
	}

	@Override
	public void close() throws IOException {
		this.serverSocket.close();
		if (this.clientSocket != null) {
			this.clientSocket.close();
		}
	}
}
