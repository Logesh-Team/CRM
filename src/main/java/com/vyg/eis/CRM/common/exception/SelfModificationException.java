package com.vyg.eis.CRM.common.exception;

public class SelfModificationException extends RuntimeException {
    public SelfModificationException(String message) {
        super(message);
    }
}
