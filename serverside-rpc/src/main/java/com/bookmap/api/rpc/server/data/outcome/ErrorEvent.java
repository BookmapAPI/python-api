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

	public ErrorEvent(String alias, int messageCodeError, String errorString, long requestId) {
		super(Type.ERROR, alias);
		this.messageCodeError = messageCodeError;
		this.errorString = errorString;
		this.requestId = requestId;
	}

	@Override
	public String toString() {
		return "ErrorEvent{" +
				"messageCodeError=" + messageCodeError +
				", errorString='" + errorString + '\'' +
				", requestId=" + requestId +
				", alias='" + alias + '\'' +
				", type=" + type +
				'}';
	}
}
