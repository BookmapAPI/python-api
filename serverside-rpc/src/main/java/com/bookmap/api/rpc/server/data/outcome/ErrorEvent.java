package com.bookmap.api.rpc.server.data.outcome;

import com.bookmap.api.rpc.server.data.utils.AbstractEventWithAlias;
import com.bookmap.api.rpc.server.data.utils.Type;

/**
 * Events responsible for reading messages
 */
public class ErrorEvent extends AbstractEventWithAlias {

	public final int messageCodeError;
	public final String errorString;
	public final long requestId;
	// Whether this error should crash the addon (SendingEventToClientHandler throws) or just be logged.
	// Expected races (e.g. an in-flight event arriving right after the instrument got detached) are not fatal.
	public final boolean fatal;

	public ErrorEvent(String alias, int messageCodeError, String errorString, long requestId) {
		this(alias, messageCodeError, errorString, requestId, true);
	}

	public ErrorEvent(String alias, int messageCodeError, String errorString, long requestId, boolean fatal) {
		super(Type.ERROR, alias);
		this.messageCodeError = messageCodeError;
		this.errorString = errorString;
		this.requestId = requestId;
		this.fatal = fatal;
	}

	@Override
	public String toString() {
		return "ErrorEvent{" +
				"messageCodeError=" + messageCodeError +
				", errorString='" + errorString + '\'' +
				", requestId=" + requestId +
				", alias='" + alias + '\'' +
				", type=" + type +
				", fatal=" + fatal +
				'}';
	}
}
