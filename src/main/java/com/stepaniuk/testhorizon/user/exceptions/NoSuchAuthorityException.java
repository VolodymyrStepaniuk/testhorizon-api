package com.stepaniuk.testhorizon.user.exceptions;

import com.stepaniuk.testhorizon.types.user.AuthorityName;
import lombok.Getter;
/**
 * Exception thrown when no authority is found in the database
 */
@Getter
public class NoSuchAuthorityException extends RuntimeException{
    private final AuthorityName authorityName;

    public NoSuchAuthorityException(AuthorityName authorityName){
        super("No authority with name " + authorityName + " found");
        this.authorityName = authorityName;
    }
}
