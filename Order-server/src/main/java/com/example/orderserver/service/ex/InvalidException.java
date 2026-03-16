package com.example.orderserver.service.ex;

public class InvalidException extends RuntimeException {
    public InvalidException(String massage) {
        super(massage);
    }

}
