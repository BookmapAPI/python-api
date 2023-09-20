package com.bookmap.api.rpc.server.log;

public class PythonLogger {

	/**
	 * This field should be set to the log category at the start of the program.
	 */
	public static final String CATEGORY = "PYTHON-CLIENT";

	public static void info(String message) {
		velox.api.layer1.common.Log.info(CATEGORY, message);
	}

	public static void info(String message, Exception ex) {
		velox.api.layer1.common.Log.info(CATEGORY, message, ex);
	}

	public static void warn(String message) {
		velox.api.layer1.common.Log.warn(CATEGORY, message);
	}

	public static void warn(String message, Throwable ex) {
		velox.api.layer1.common.Log.warn(CATEGORY, message, ex);
	}

	public static void error(String message) {
		velox.api.layer1.common.Log.error(CATEGORY, message);
	}

	public static void error(String message, Throwable ex) {
		if (ex instanceof Exception) {
			velox.api.layer1.common.Log.error(CATEGORY, message, (Exception) ex);
		} else {
			// There's no overload taking category and Throwable at the same time, so let's just prepend message
			// with the category.
			velox.api.layer1.common.Log.error(String.format("[%s] %s", CATEGORY, message), ex);
		}
	}

	public static void debug(String message) {
		velox.api.layer1.common.Log.debug(CATEGORY, message);
	}

	public static void trade(String message) {
		velox.api.layer1.common.Log.trade(CATEGORY, message);
	}
}
