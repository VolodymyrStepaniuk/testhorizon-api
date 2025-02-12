package com.stepaniuk.testhorizon.shared.exceptions;

public class ProducerException extends RuntimeException {

    public ProducerException(Throwable cause) {
        super("Exception occurred in producer", cause);
    }
}
