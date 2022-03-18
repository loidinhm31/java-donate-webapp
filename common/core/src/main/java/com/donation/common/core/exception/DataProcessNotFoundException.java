package com.donation.common.core.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class DataProcessNotFoundException extends Exception {

    private final HttpStatus errorCode;

    public DataProcessNotFoundException(HttpStatus errorCode, String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
        this.errorCode = errorCode;
    }

    public DataProcessNotFoundException(HttpStatus errorCode, String detailMessage) {
        super(detailMessage);
        this.errorCode = errorCode;
    }

    public DataProcessNotFoundException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
        this.errorCode = null;
    }

    public DataProcessNotFoundException(String detailMessage) {
        super(detailMessage);
        this.errorCode = null;
    }

    public DataProcessNotFoundException(Throwable throwable) {
        super(throwable);
        this.errorCode = null;
    }

    public DataProcessNotFoundException() {
        this.errorCode = null;
    }

    public HttpStatus getErrorCode() {
        return errorCode;
    }
}
