package com.filmbooking.booking_service.utils.user.role;

public class NoRole implements Role {

    @Override
    public boolean sameAs(Role r) {
        return false;
    }

}
