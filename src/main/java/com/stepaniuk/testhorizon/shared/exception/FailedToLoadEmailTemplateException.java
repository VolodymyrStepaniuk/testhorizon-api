package com.stepaniuk.testhorizon.shared.exception;

public class FailedToLoadEmailTemplateException extends RuntimeException{

        public FailedToLoadEmailTemplateException(Throwable cause) {
            super("Failed to load email template", cause);
        }

}
