package com.bookmap.api.rpc.server.exceptions;

public class FailedToStartServerException extends Exception {

	public FailedToStartServerException(String msg) {
		super(msg);
	}

	public FailedToStartServerException(Exception ex) {
		super(ex);
	}
}
