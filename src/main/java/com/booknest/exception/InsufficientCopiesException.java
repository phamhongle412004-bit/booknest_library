package com.booknest.exception;

public class InsufficientCopiesException extends RuntimeException {
    public InsufficientCopiesException(String message) { super(message); }
}