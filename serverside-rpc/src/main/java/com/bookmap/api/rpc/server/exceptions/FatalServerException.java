package com.bookmap.api.rpc.server.exceptions;

public class FatalServerException extends RuntimeException {

	public FatalServerException(String msg) {
		super(msg);
	}

	public FatalServerException(Exception ex) {
		super(ex);
	}
}
