package com.stepaniuk.testhorizon.shared;

import com.stepaniuk.testhorizon.bugreport.exceptions.NoSuchBugReportByIdException;
import com.stepaniuk.testhorizon.bugreport.exceptions.NoSuchBugReportSeverityByNameException;
import com.stepaniuk.testhorizon.bugreport.exceptions.NoSuchBugReportStatusByNameException;
import com.stepaniuk.testhorizon.payload.comment.exception.CommentAuthorMismatchException;
import com.stepaniuk.testhorizon.payload.comment.exception.NoSuchCommentByIdException;
import com.stepaniuk.testhorizon.project.exception.NoSuchProjectByIdException;
import com.stepaniuk.testhorizon.project.exception.NoSuchProjectStatusByNameException;
import com.stepaniuk.testhorizon.rating.exceptions.UserCannotChangeOwnRatingException;
import com.stepaniuk.testhorizon.security.auth.passwordreset.exception.NoSuchPasswordResetTokenException;
import com.stepaniuk.testhorizon.security.auth.passwordreset.exception.PasswordResetTokenExpiredException;
import com.stepaniuk.testhorizon.security.exceptions.InvalidTokenException;
import com.stepaniuk.testhorizon.test.exceptions.NoSuchTestByIdException;
import com.stepaniuk.testhorizon.test.exceptions.NoSuchTestTypeByNameException;
import com.stepaniuk.testhorizon.testcase.exceptions.NoSuchTestCaseByIdException;
import com.stepaniuk.testhorizon.testcase.exceptions.NoSuchTestCasePriorityByNameException;
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

    @ExceptionHandler(value = {NoSuchBugReportByIdException.class})
    public ProblemDetail handleNoSuchBugReportByIdException(NoSuchBugReportByIdException e) {
        var problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND,
                "No bug report with id " + e.getId());
        problemDetail.setTitle("No such bug report");
        problemDetail.setInstance(URI.create("/bug-reports"));
        return problemDetail;
    }

    @ExceptionHandler(value = {NoSuchBugReportSeverityByNameException.class})
    public ProblemDetail handleNoSuchBugReportSeverityByNameException(NoSuchBugReportSeverityByNameException e) {
        var problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND,
                "No bug report severity with name " + e.getName());
        problemDetail.setTitle("No such bug report severity");
        problemDetail.setInstance(URI.create("/bug-reports"));
        return problemDetail;
    }

    @ExceptionHandler(value = {NoSuchBugReportStatusByNameException.class})
    public ProblemDetail handleNoSuchBugReportStatusByNameException(NoSuchBugReportStatusByNameException e) {
        var problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND,
                "No bug report status with name " + e.getName());
        problemDetail.setTitle("No such bug report status");
        problemDetail.setInstance(URI.create("/bug-reports"));
        return problemDetail;
    }

    @ExceptionHandler(value = {NoSuchTestCaseByIdException.class})
    public ProblemDetail handleNoSuchTestCaseByIdException(NoSuchTestCaseByIdException e) {
        var problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND,
                "No test case with id " + e.getId());
        problemDetail.setTitle("No such test case");
        problemDetail.setInstance(URI.create("/test-cases"));
        return problemDetail;
    }

    @ExceptionHandler(value = {NoSuchTestCasePriorityByNameException.class})
    public ProblemDetail handleNoSuchTestCasePriorityByNameException(NoSuchTestCasePriorityByNameException e) {
        var problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND,
                "No test case priority with name " + e.getName());
        problemDetail.setTitle("No such test case priority");
        problemDetail.setInstance(URI.create("/test-cases"));
        return problemDetail;
    }

    @ExceptionHandler(value = {NoSuchAuthorityException.class})
    public ProblemDetail handleNoSuchAuthorityException(NoSuchAuthorityException e) {
        var problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND,
                "No authority with name " + e.getAuthorityName());
        problemDetail.setTitle("No such authority");
        problemDetail.setInstance(URI.create("/users"));
        return problemDetail;
    }

    @ExceptionHandler(value = {InvalidTokenException.class})
    public ProblemDetail handleInvalidTokenException(InvalidTokenException e) {
        var problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                "Invalid token: " + e.getToken());
        problemDetail.setTitle("Invalid token");
        problemDetail.setInstance(URI.create("/auth/refresh"));
        return problemDetail;
    }

    @ExceptionHandler(value = {NoSuchTestTypeByNameException.class})
    public ProblemDetail handleNoSuchTestTypeByNameException(NoSuchTestTypeByNameException e) {
        var problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND,
                "No test type with name " + e.getName());
        problemDetail.setTitle("No such test type");
        problemDetail.setInstance(URI.create("/tests"));
        return problemDetail;
    }

    @ExceptionHandler(value = {NoSuchTestByIdException.class})
    public ProblemDetail handleNoSuchTestByIdException(NoSuchTestByIdException e) {
        var problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND,
                "No test with id " + e.getId());
        problemDetail.setTitle("No such test");
        problemDetail.setInstance(URI.create("/tests"));
        return problemDetail;
    }

    @ExceptionHandler(value = {NoSuchCommentByIdException.class})
    public ProblemDetail handleNoSuchCommentByIdException(NoSuchCommentByIdException e) {
        var problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND,
                "No comment with id " + e.getCommentId());
        problemDetail.setTitle("No such comment");
        problemDetail.setInstance(URI.create("/comments"));
        return problemDetail;
    }

    @ExceptionHandler(value = {CommentAuthorMismatchException.class})
    public ProblemDetail handleCommentAuthorMismatchException(CommentAuthorMismatchException e) {
        var problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN,
                "Comment author mismatch: " + e.getCommentId());
        problemDetail.setTitle("Comment author mismatch");
        problemDetail.setInstance(URI.create("/comments"));
        return problemDetail;
    }

    @ExceptionHandler(value = {UserCannotChangeOwnRatingException.class})
    public ProblemDetail handleUserCannotChangeOwnRatingException(UserCannotChangeOwnRatingException e) {
        var problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN,
                "User cannot change own rating: " + e.getUserId());
        problemDetail.setTitle("User cannot change own rating");
        problemDetail.setInstance(URI.create("/ratings"));
        return problemDetail;
    }

    @ExceptionHandler(value = {NoSuchPasswordResetTokenException.class})
    public ProblemDetail handleNoSuchPasswordResetTokenException(NoSuchPasswordResetTokenException e) {
        var problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND,
                "No password reset token with token: " + e.getToken());
        problemDetail.setTitle("No such password reset token");
        problemDetail.setInstance(URI.create("/auth/reset-password"));
        return problemDetail;
    }

    @ExceptionHandler(value = {PasswordResetTokenExpiredException.class})
    public ProblemDetail handlePasswordResetTokenExpiredException(PasswordResetTokenExpiredException e) {
        var problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                "Password reset token expired: " + e.getToken());
        problemDetail.setTitle("Password reset token expired");
        problemDetail.setInstance(URI.create("/auth/reset-password"));
        return problemDetail;
    }

}
