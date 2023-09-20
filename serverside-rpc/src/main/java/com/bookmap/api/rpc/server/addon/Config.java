package com.bookmap.api.rpc.server.addon;

import com.bookmap.api.rpc.server.NetworkUtils;
import com.bookmap.api.rpc.server.log.RpcLogger;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class Config {

	private static final Properties PROPERTIES = new Properties();

	static {
		try (var stream  = Config.class.getClassLoader().getResourceAsStream("addon.properties")) {
			PROPERTIES.load(stream);
		} catch (FileNotFoundException e) {
			RpcLogger.error("File not found", e);
			throw new IllegalStateException("Failed to read properties, file not found...");
		} catch (IOException e) {
			RpcLogger.error("Failed to read file", e);
			throw new RuntimeException(e);
		}
	}

	public static String getPythonRuntime() {
		return PROPERTIES.getProperty("python_runtime");
	}

	public static int getTcpPort() {
		String configuredPort = PROPERTIES.getProperty("tcp_port");
		if ("any".equalsIgnoreCase(configuredPort)) {
			return NetworkUtils.getFreeTcpPort(32132, 65000);
		}

		return Integer.parseInt(configuredPort);
	}
}
