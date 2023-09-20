package com.bookmap.python.api.addon.exceptions;

public class FailedToBuildException extends Exception {

    public FailedToBuildException(Exception ex) {
        super(ex);
    }

    public FailedToBuildException(String error, Exception ex) {
        super(error, ex);
    }

    public FailedToBuildException(String error) {
        super(error);
    }
}
