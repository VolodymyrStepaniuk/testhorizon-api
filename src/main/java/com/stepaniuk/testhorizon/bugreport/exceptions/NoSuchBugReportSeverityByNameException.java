package com.stepaniuk.testhorizon.bugreport.exceptions;

import com.stepaniuk.testhorizon.bugreport.severity.BugReportSeverityName;
import lombok.Getter;

/**
 * Exception thrown when there is no bug report severity with the given name.
 */
@Getter
public class NoSuchBugReportSeverityByNameException extends RuntimeException{

    private final BugReportSeverityName name;

    public NoSuchBugReportSeverityByNameException(BugReportSeverityName name) {
        super("No bug report severity with name: " + name);
        this.name = name;
    }
}
