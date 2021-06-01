package com.filmbooking.booking_service.utils.permission;

import com.filmbooking.booking_service.utils.permission.operation.Operation;

public class NoPermission implements Permissions {

    @Override
    public boolean allow(Operation op) {
        return false;
    }

    @Override
    public boolean forbid(Operation op) {
        return true;
    }

}
