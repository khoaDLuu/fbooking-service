package com.filmbooking.booking_service.utils.user;

public class NullUser implements User {

    @Override
    public String name() {
        return "";
    }

    @Override
    public String roles() {
        return "NO_ROLES";
    }

}
