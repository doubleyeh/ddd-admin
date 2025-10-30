package com.mok.ddd.application.exception;

public class BizException extends RuntimeException{
    public BizException(String message){
        super(message);
    }
}
