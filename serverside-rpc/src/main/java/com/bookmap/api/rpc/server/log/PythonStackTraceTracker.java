package com.bookmap.api.rpc.server.log;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 * This tracker is used to track python stacktrace from standard error stream.
 * It is used to show users the last `traceLength` lines of python stacktrace if Python script failed.
 * E.g. if there is some typo, wrong method name etc.
 */
public class PythonStackTraceTracker {

    private static final int DEFAULT_TRACE_LENGTH = 30;
    private int traceLength;
    private final Queue<String> stackTrace;
    private static PythonStackTraceTracker instance = null;

    public static synchronized PythonStackTraceTracker getTracker() {
        if (instance == null) {
            instance = new PythonStackTraceTracker();
        }
        return instance;
    }

    private PythonStackTraceTracker() {
        this.traceLength = DEFAULT_TRACE_LENGTH;
        stackTrace = new ArrayDeque<>(traceLength);
    }

    public String get() {
        StringBuilder sb = new StringBuilder();
        while (!stackTrace.isEmpty()) {
            sb.append(stackTrace.poll()).append("\n");
        }
        return sb.toString();
    }

    public void addErrorLine(String line) {
        if (stackTrace.size() >= traceLength) {
            stackTrace.poll();
        }
        stackTrace.offer(line);
    }

    public void setTraceLength(int traceLength) {
        this.traceLength = traceLength;
    }
}
