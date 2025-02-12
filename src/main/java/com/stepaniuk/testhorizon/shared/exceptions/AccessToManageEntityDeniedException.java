package com.stepaniuk.testhorizon.shared.exceptions;

import lombok.Getter;

/**
 * Exception thrown when user tries to manage entity without permission
 */
@Getter
public class AccessToManageEntityDeniedException extends RuntimeException{

    private final String entityName;
    private final String entityUrl;

    public AccessToManageEntityDeniedException(String entityName, String entityUrl) {
        super("Access to manage entity \"" + entityName + "\" denied");
        this.entityName = entityName;
        this.entityUrl = entityUrl;
    }

}
