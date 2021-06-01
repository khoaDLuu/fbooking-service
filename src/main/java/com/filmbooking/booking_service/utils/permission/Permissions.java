package com.filmbooking.booking_service.utils.permission;

import com.filmbooking.booking_service.utils.permission.operation.Operation;

public interface Permissions {
    public boolean allow(Operation op);
    public boolean forbid(Operation op);
}
