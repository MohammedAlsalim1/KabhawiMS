package com.example.cartserver.service.ex;

public class AlreadyException extends RuntimeException {
    public AlreadyException(String massage) {
        super(massage);
    }
}
