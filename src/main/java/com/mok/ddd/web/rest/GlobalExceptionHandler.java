package com.mok.ddd.web.rest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.security.access.AccessDeniedException;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({UsernameNotFoundException.class, BadCredentialsException.class})
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public RestResponse<?> handleAuthenticationException(RuntimeException e) {
        log.warn("Authentication failed: {}", e.getMessage());
        return RestResponse.failure(401, "用户名或密码错误");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(value = HttpStatus.OK)
    public RestResponse<?> handleValidationExceptions(MethodArgumentNotValidException e) {
        String message = Objects.requireNonNull(e.getBindingResult().getFieldError()).getDefaultMessage();
        log.warn("Validation failed: {}", message);
        return RestResponse.failure(400, message);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public RestResponse<String> handleAccessDenied(AccessDeniedException ex) {
        return RestResponse.failure(HttpStatus.FORBIDDEN.value(), "权限不足，拒绝访问");
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(value = HttpStatus.OK)
    public RestResponse<String> handleGeneralException(Exception ex) {
        log.error("Unhandled exception occurred:", ex);
        return RestResponse.failure(HttpStatus.INTERNAL_SERVER_ERROR.value(), "系统繁忙，请稍后重试");
    }
}