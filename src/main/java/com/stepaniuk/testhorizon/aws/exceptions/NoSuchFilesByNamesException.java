package com.stepaniuk.testhorizon.aws.exceptions;

import lombok.Getter;

import java.util.List;

/**
 * Exception thrown when no file with given name is found
 */
@Getter
public class NoSuchFilesByNamesException extends RuntimeException{

    private final List<String> fileNames;

    public NoSuchFilesByNamesException(List<String> fileNames) {
        super("No files with names: " + fileNames + " found");
        this.fileNames = fileNames;
    }
}
