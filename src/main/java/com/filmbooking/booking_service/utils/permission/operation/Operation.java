package com.filmbooking.booking_service.utils.permission.operation;

public interface Operation {
    public String name();
    public boolean sameAs(Operation op);
}
