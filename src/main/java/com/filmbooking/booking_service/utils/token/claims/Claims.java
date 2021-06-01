package com.filmbooking.booking_service.utils.token.claims;

import com.filmbooking.booking_service.utils.permission.Permissions;
import com.filmbooking.booking_service.utils.user.User;

public interface Claims {
    public Permissions perms();
    public User requester();
}
