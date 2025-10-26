package com.example.productserver.service.ex;

public class InvalidException extends RuntimeException {
    public InvalidException(String massage) {
        super(massage);
    }

}
