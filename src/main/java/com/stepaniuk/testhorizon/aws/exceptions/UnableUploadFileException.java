package com.stepaniuk.testhorizon.aws.exceptions;

import lombok.Getter;

/**
 * Exception thrown when unable to upload file to S3.
 */
@Getter
public class UnableUploadFileException extends RuntimeException{

    private final String fileName;

    public UnableUploadFileException(String fileName) {
        super("Unable to upload file: " + fileName);
        this.fileName = fileName;
    }
}
