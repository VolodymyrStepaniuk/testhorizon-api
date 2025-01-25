package com.stepaniuk.testhorizon.shared.exception;

public class ProducerException extends RuntimeException {

    public ProducerException(Throwable cause) {
        super("Exception occurred in producer", cause);
    }
}
