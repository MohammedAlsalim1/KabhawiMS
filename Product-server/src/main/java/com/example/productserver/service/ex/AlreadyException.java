package com.example.productserver.service.ex;

public class AlreadyException extends RuntimeException {
    public AlreadyException(String massage) {
        super(massage);
    }
}
