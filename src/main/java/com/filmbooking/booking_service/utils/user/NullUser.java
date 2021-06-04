package com.filmbooking.booking_service.utils.user;

import com.filmbooking.booking_service.utils.user.role.NoRole;
import com.filmbooking.booking_service.utils.user.role.Role;

public class NullUser implements User {

    @Override
    public Long id() {
        return Long.valueOf(0);
    }

    @Override
    public String name() {
        return "";
    }

    @Override
    public Role roles() {
        return new NoRole();
    }

}
