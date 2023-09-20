package com.bookmap.api.rpc.server;

import com.bookmap.api.rpc.server.log.PythonLogger;
import com.bookmap.api.rpc.server.log.PythonStackTraceTracker;
import velox.api.layer1.common.Log;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This is wrapper for logs received from extended systems which should be read and formatted in Bookmap way.
 * This class works asynchronously, so no need to worry about that main thread will be blocking. This instance of this class
 * are supposed to be used once for one input stream. After reading is finished, object can be released and can not be used again.
 */
public class LogTracker {

	private static ExecutorService EXECUTOR_SERVICE;

	public static void track(Log.LogLevel logLevel, InputStream inputStream) {
		EXECUTOR_SERVICE.execute(() -> {
			PythonStackTraceTracker traceTracker = PythonStackTraceTracker.getTracker();
			try (var reader = new BufferedReader(new InputStreamReader(inputStream))) {
				String bufferLine;
				while ((bufferLine = reader.readLine()) != null) {
					switch (logLevel) {
						case INFO -> PythonLogger.info(bufferLine);
						case WARN -> PythonLogger.warn(bufferLine);
						case DEBUG -> PythonLogger.debug(bufferLine);
						case ERROR -> {
							PythonLogger.error(bufferLine);
							traceTracker.addErrorLine(bufferLine);
						}
						case TRADING -> PythonLogger.trade(bufferLine);
					}
				}
			} catch (IOException e) {
				Log.error("Failed to track logs from the stream", e);
			}
		});
	}

	public static void initExecutorService() {
		EXECUTOR_SERVICE = Executors.newCachedThreadPool();
	}

	public static void finish() {
		EXECUTOR_SERVICE.shutdownNow();
	}
}
