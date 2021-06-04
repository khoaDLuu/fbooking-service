package com.filmbooking.booking_service.utils.user.role;

import java.util.Objects;

public class SimpleRole implements Role {

    private String name;

    public SimpleRole(String rolename) {
        this.name = rolename;
    }

    @Override
    public boolean sameAs(Role r) {
        return this.equals(r);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof SimpleRole))
            return false;

        SimpleRole simRole = (SimpleRole) o;
        return Objects.equals(this.name, simRole.name);
    }

}
