package com.stepaniuk.testhorizon.shared;

import com.stepaniuk.testhorizon.project.exception.NoSuchProjectByIdException;
import com.stepaniuk.testhorizon.project.exception.NoSuchProjectStatusByNameException;
import com.stepaniuk.testhorizon.user.email.exceptions.InvalidVerificationCodeException;
import com.stepaniuk.testhorizon.user.email.exceptions.VerificationCodeExpiredException;
import com.stepaniuk.testhorizon.user.exceptions.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;

@RestControllerAdvice
public class GeneralControllerExceptionHandler {

    @ExceptionHandler(value = {InvalidVerificationCodeException.class})
    public ProblemDetail handleInvalidVerificationCodeException(InvalidVerificationCodeException e) {
        var problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                "Invalid verification code for " + e.getEmail());
        problemDetail.setTitle("Invalid verification code");
        problemDetail.setInstance(URI.create("/auth/verify"));
        return problemDetail;
    }

    @ExceptionHandler(value = {VerificationCodeExpiredException.class})
    public ProblemDetail handleVerificationCodeExpiredException(VerificationCodeExpiredException e) {
        var problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                "Verification code expired: " + e.getCode());
        problemDetail.setTitle("Verification code expired");
        problemDetail.setInstance(URI.create("/auth/verify"));
        return problemDetail;
    }

    @ExceptionHandler(value = {NoSuchUserByEmailException.class})
    public ProblemDetail handleNoSuchUserByEmailException(NoSuchUserByEmailException e) {
        var problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND,
                "No user with email " + e.getEmail());
        problemDetail.setTitle("No such user");
        problemDetail.setInstance(URI.create("/users"));
        return problemDetail;
    }

    @ExceptionHandler(value = {NoSuchUserByIdException.class})
    public ProblemDetail handleNoSuchUserByIdException(NoSuchUserByIdException e) {
        var problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND,
                "No user with id " + e.getId());
        problemDetail.setTitle("No such user");
        problemDetail.setInstance(URI.create("/users"));
        return problemDetail;
    }

    @ExceptionHandler(value = {UserAlreadyExistsException.class})
    public ProblemDetail handleUserAlreadyExistsException(UserAlreadyExistsException e) {
        var problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
                "User with email " + e.getEmail() + " already exists");
        problemDetail.setTitle("User already exists");
        problemDetail.setInstance(URI.create("/users"));
        return problemDetail;
    }

    @ExceptionHandler(value = {UserAlreadyVerifiedException.class})
    public ProblemDetail handleUserAlreadyVerifiedException(UserAlreadyVerifiedException e) {
        var problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                "User with email " + e.getEmail() + " is already verified");
        problemDetail.setTitle("User already verified");
        problemDetail.setInstance(URI.create("/users"));
        return problemDetail;
    }

    @ExceptionHandler(value = {UserNotVerifiedException.class})
    public ProblemDetail handleUserNotVerifiedException(UserNotVerifiedException e) {
        var problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                "User with email " + e.getEmail() + " is not verified");
        problemDetail.setTitle("User not verified");
        problemDetail.setInstance(URI.create("/users"));
        return problemDetail;
    }

    @ExceptionHandler(value = {NoSuchProjectByIdException.class})
    public ProblemDetail handleNoSuchProjectByIdException(NoSuchProjectByIdException e) {
        var problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND,
                "No project with id " + e.getId());
        problemDetail.setTitle("No such project");
        problemDetail.setInstance(URI.create("/projects"));
        return problemDetail;
    }

    @ExceptionHandler(value = {NoSuchProjectStatusByNameException.class})
    public ProblemDetail handleNoSuchProjectStatusByNameException(NoSuchProjectStatusByNameException e) {
        var problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND,
                "No project status with name " + e.getName());
        problemDetail.setTitle("No such project status");
        problemDetail.setInstance(URI.create("/projects"));
        return problemDetail;
    }

}
