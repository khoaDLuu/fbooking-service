package com.filmbooking.booking_service;

import java.util.List;

public class ResponseWrapper<T> {
    private List<T> data;

    public ResponseWrapper(List<T> data) {
        this.data = data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }

    public List<T> getData() {
        return this.data;
    }
}
