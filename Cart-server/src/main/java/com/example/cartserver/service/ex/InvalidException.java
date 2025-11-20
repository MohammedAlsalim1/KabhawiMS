package com.example.cartserver.service.ex;

public class InvalidException extends RuntimeException {
    public InvalidException(String massage) {
        super(massage);
    }

}
