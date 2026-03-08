package com.erc.dal;

public class DALException extends RuntimeException {

    public DALException(String message) {
        super(message);
    }

    public DALException(String message, Throwable cause) {
        super(message, cause);
    }
}
