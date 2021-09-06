package com.mou.ppm.exceptions;

public class ProjectNotFoundException extends BusinessLogicException {
    public ProjectNotFoundException(String message) {
        super(message);
    }
}
