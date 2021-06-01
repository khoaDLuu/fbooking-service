package com.filmbooking.booking_service.utils.token.claims;

import com.filmbooking.booking_service.utils.permission.NoPermission;
import com.filmbooking.booking_service.utils.permission.Permissions;
import com.filmbooking.booking_service.utils.user.NullUser;
import com.filmbooking.booking_service.utils.user.User;

public class EmptyClaims implements Claims {

    @Override
    public Permissions perms() {
        return new NoPermission();
    }

    @Override
    public User requester() {
        return new NullUser();
    }

}
