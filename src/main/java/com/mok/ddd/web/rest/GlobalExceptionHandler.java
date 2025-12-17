package com.mok.ddd.web.rest;

import com.mok.ddd.application.exception.BizException;
import com.mok.ddd.application.exception.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

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

    @ExceptionHandler(AuthorizationDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public RestResponse<?> handleAuthorizationDeniedException(AuthorizationDeniedException e) {
        log.warn("AuthorizationDeniedException failed: {}", e.getMessage());
        return RestResponse.failure(403, "您没有权限执行该操作");
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

    @ExceptionHandler(BizException.class)
    @ResponseStatus(value = HttpStatus.OK)
    public RestResponse<String> handleBizException(BizException ex) {
        log.error("BizException exception occurred:", ex);
        return RestResponse.failure(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.getMessage());
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(value = HttpStatus.OK)
    public RestResponse<String> handleNotFoundException(NotFoundException ex) {
        log.error("NotFoundException exception occurred:", ex);
        return RestResponse.failure(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(value = HttpStatus.OK)
    public RestResponse<String> handleGeneralException(Exception ex) {
        log.error("Unhandled exception occurred:", ex);
        return RestResponse.failure(HttpStatus.INTERNAL_SERVER_ERROR.value(), "系统繁忙，请稍后重试");
    }
}