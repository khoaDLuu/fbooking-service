package com.filmbooking.booking_service;

public class ResponseWrapperSingle<T> {
    private T data;

    public ResponseWrapperSingle(T data) {
        this.data = data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public T getData() {
        return this.data;
    }
}
