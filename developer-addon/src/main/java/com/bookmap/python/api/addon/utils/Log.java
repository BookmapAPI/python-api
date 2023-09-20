package com.bookmap.python.api.addon.utils;

import java.io.PrintStream;
import java.time.Instant;

/**
 * This is a pass-through class that adds a category to all the Log calls
 * <p>
 * Having the logs annotated with meaningful category makes it much easier to debug user issues.
 */
public class Log {
    static {
        if ("1".equals(System.getenv("BOOKMAP_COLOR_LOG"))) {
            // This overwrites the default listener and makes the logs not be written to the logs file (or be available
            // in Bookmap under File), so it must never be set for the end users. Only for developers.
            velox.api.layer1.common.Log.setListener(new ColorLogListener());
        }
    }

    /**
     * This field should be set to the log category at the start of the program.
     */
    public static String category = "PYTHON-EDITOR";

    public static void info(String message) {
        velox.api.layer1.common.Log.info(category, message);
    }

    public static void info(String message, Exception ex) {
        velox.api.layer1.common.Log.info(category, message, ex);
    }

    public static void warn(String message) {
        velox.api.layer1.common.Log.warn(category, message);
    }

    public static void warn(String message, Throwable ex) {
        velox.api.layer1.common.Log.warn(category, message, ex);
    }

    public static void error(String message) {
        velox.api.layer1.common.Log.error(category, message);
    }

    public static void error(String message, Throwable ex) {
        if (ex instanceof Exception) {
            velox.api.layer1.common.Log.error(category, message, (Exception) ex);
        } else {
            // There's no overload taking category and Throwable at the same time, so let's just prepend message
            // with the category.
            velox.api.layer1.common.Log.error(String.format("[%s] %s", category, message), ex);
        }
    }

    public static void debug(String msg) {
        velox.api.layer1.common.Log.debug(category, msg);
    }

    public static void trade(String message) {
        velox.api.layer1.common.Log.trade(category, message);
    }

    private static class ColorLogListener implements velox.api.layer1.common.Log.LogListener {

        private static final String ANSI_RESET = "\u001B[0m";
        private static final String ANSI_RED = "\u001B[31m";
        private static final String ANSI_GREEN = "\u001B[32m";
        private static final String ANSI_YELLOW = "\u001B[33m";
        private static final String ANSI_PURPLE = "\u001B[35m";
        private static final String ANSI_CYAN = "\u001B[36m";
        private static final String ANSI_WHITE = "\u001B[37m";
        private static final PrintStream out = System.out;

        @Override
        public void log(
            velox.api.layer1.common.Log.LogLevel logLevel,
            String category,
            String message,
            Throwable throwable
        ) {
            synchronized (out) {
                out.print(ANSI_CYAN);
                out.print(Instant.now());
                out.print(ANSI_RESET);
                out.print(' ');
                if (logLevel == velox.api.layer1.common.Log.LogLevel.ERROR) {
                    out.print(ANSI_RED);
                } else if (logLevel == velox.api.layer1.common.Log.LogLevel.WARN) {
                    out.print(ANSI_YELLOW);
                } else if (logLevel == velox.api.layer1.common.Log.LogLevel.INFO) {
                    out.print(ANSI_GREEN);
                } else {
                    out.print(ANSI_WHITE);
                }
                out.print(logLevel);
                out.print(ANSI_RESET);
                out.print(": ");
                if (category != null) {
                    out.print('[');
                    out.print(ANSI_PURPLE);
                    out.print(category);
                    out.print(ANSI_RESET);
                    out.print("] ");
                }
                out.println(message);
                if (throwable != null) {
                    throwable.printStackTrace(out);
                }
            }
        }
    }
}
