package com.example.orderserver.service.ex;

public class NotExistException extends RuntimeException {
    public NotExistException(String massage) {
        super(massage);
    }
}
