package com.stepaniuk.testhorizon.project.exception;

import com.stepaniuk.testhorizon.project.status.ProjectStatusName;
import lombok.Getter;

/**
 * Exception thrown when project status with given name does not exist.
 */
@Getter
public class NoSuchProjectStatusByNameException extends RuntimeException{

    private final ProjectStatusName name;

    public NoSuchProjectStatusByNameException(ProjectStatusName name) {
        super("Project status with name " + name + " does not exist");
        this.name = name;
    }
}
