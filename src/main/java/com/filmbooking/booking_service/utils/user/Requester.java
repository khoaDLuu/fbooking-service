package com.filmbooking.booking_service.utils.user;

import com.filmbooking.booking_service.utils.user.role.Role;
import com.filmbooking.booking_service.utils.user.role.SimpleRole;

public class Requester implements User {

    private Long id;
    private String name;
    private String roles;

    public Requester(Long id, String name, String roles) {
        this.id = id;
        this.name = name;
        this.roles = roles;
    }

    @Override
    public Long id() {
        return this.id;
    }

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public Role roles() {
        return new SimpleRole(this.roles);
    }

}
