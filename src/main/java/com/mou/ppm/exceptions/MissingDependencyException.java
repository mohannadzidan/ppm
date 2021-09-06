package com.mou.ppm.exceptions;

public class MissingDependencyException extends BusinessLogicException {
    public MissingDependencyException(String message) {
        super(message);
    }
}
