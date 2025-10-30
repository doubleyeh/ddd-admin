package com.mok.ddd.application.exception;

public class NotFoundException extends RuntimeException{
    public NotFoundException(){
        super("数据不存在");
    }

    public NotFoundException(String message){
        super(message);
    }
}
