package com.master.milano.exception.item;

public class UserAlreadyInterestInItem extends RuntimeException{
    public UserAlreadyInterestInItem(String message) {
        super(message);
    }
}
