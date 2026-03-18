package com.example.orderserver.controller.advice;


import com.example.orderserver.controller.OrderController;
import com.example.orderserver.service.ex.AlreadyException;
import com.example.orderserver.service.ex.EmptyException;
import com.example.orderserver.service.ex.InvalidException;
import com.example.orderserver.service.ex.NotExistException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackageClasses = OrderController.class)
public class OrderControllerAdvice {
    @ExceptionHandler(EmptyException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ProblemDetail handleEmpty(EmptyException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(NotExistException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ProblemDetail handleNotExist(NotExistException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(AlreadyException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ProblemDetail handleAlreadyAssigned(AlreadyException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(InvalidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ProblemDetail handleInvalidException(InvalidException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

}