package com.khazhimetov.library.exception;

/**
 * Inactive account exception.
 */
public class InactiveAccountException extends ServiceException {
    public InactiveAccountException() {
        super("error.account.inactive");
    }
}
