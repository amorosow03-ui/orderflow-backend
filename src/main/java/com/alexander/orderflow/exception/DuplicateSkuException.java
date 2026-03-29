package com.alexander.orderflow.exception;

public class DuplicateSkuException extends RuntimeException{
    public DuplicateSkuException(String message){
        super(message);
    }
}