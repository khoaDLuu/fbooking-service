package com.filmbooking.booking_service.utils.user;

import com.filmbooking.booking_service.utils.user.role.Role;

public interface User {
    public Long id();
    public String name();
    public Role roles();
}
