package com.chung.lifusic.file.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class UnExpectedException extends ResponseStatusException {
    public UnExpectedException() {
        super(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public UnExpectedException(String reason) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, reason);
    }
}
