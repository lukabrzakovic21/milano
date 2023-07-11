package com.master.milano.exception.item;

public class NoMoreItemsException extends RuntimeException{
    public NoMoreItemsException(String message) {
        super(message);
    }
}
