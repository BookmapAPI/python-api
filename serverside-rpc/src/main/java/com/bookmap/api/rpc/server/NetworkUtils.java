package com.bookmap.api.rpc.server;

import java.net.ServerSocket;

public class NetworkUtils {

	public static int getFreeTcpPort(int from, int to) {
		if (from < 0 || to > 65353) {
			throw new IllegalStateException("Port is out of available range");
		}

		if (to < from) {
			throw new IllegalStateException("Upper bound less than lower bound");
		}
		for (int i = from; i <= to; ++i) {
			try {
				ServerSocket datagramSocket = new ServerSocket(i);
				datagramSocket.close();
				return i;
			} catch (Exception ignored) {
			}
		}

		throw new IllegalStateException("No free port");
	}
}
