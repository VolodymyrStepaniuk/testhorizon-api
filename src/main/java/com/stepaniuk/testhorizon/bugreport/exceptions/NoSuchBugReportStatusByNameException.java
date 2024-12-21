package com.stepaniuk.testhorizon.bugreport.exceptions;

import com.stepaniuk.testhorizon.bugreport.status.BugReportStatusName;
import lombok.Getter;

/**
 * Exception thrown when there is no bug report status with the given name.
 */
@Getter
public class NoSuchBugReportStatusByNameException extends RuntimeException{

    private final BugReportStatusName name;

    public NoSuchBugReportStatusByNameException(BugReportStatusName name) {
        super("No bug report status with name: " + name);
        this.name = name;
    }
}
