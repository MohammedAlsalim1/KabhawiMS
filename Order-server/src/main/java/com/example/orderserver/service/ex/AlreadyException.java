package com.example.orderserver.service.ex;

public class AlreadyException extends RuntimeException {
    public AlreadyException(String massage) {
        super(massage);
    }
}
