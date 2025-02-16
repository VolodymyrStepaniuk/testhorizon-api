package com.stepaniuk.testhorizon.project.exceptions;


import com.stepaniuk.testhorizon.types.project.ProjectStatusName;
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
