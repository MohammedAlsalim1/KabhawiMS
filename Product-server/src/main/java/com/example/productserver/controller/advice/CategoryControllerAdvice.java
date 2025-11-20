package com.example.productserver.controller.advice;

import com.example.productserver.controller.CategoryController;
import com.example.productserver.service.ex.AlreadyException;
import com.example.productserver.service.ex.EmptyException;
import com.example.productserver.service.ex.InvalidException;
import com.example.productserver.service.ex.NotExistException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackageClasses = CategoryController.class)
public class CategoryControllerAdvice {
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
