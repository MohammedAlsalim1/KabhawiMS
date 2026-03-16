package com.example.orderserver.service.ex;

public class EmptyException extends RuntimeException {
    public EmptyException(String massage) {
        super(massage);
    }
}
