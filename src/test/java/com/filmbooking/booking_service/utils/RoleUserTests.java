package com.filmbooking.booking_service.utils;

import com.filmbooking.booking_service.utils.user.NullUser;
import com.filmbooking.booking_service.utils.user.Requester;
import com.filmbooking.booking_service.utils.user.User;
import com.filmbooking.booking_service.utils.user.role.NoRole;
import com.filmbooking.booking_service.utils.user.role.SimpleRole;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class RoleUserTests {

    @Test
    public void testRoleEquality() {
        Assertions.assertEquals(
            new SimpleRole("ROLE_EMPLOYEE"),
            new SimpleRole("ROLE_EMPLOYEE")
        );
        Assertions.assertTrue(
            new SimpleRole("ROLE_EMPLOYEE").sameAs(
                new SimpleRole("ROLE_EMPLOYEE")
            )
        );
    }

    @Test
    public void testNoRoleNotEqualToItself() {
        Assertions.assertNotEquals(
            new NoRole(),
            new NoRole()
        );
        Assertions.assertFalse(new NoRole().sameAs(new NoRole()));
    }

    @Test
    public void testRoleOfNullUser() {
        User noUser = new NullUser();
        Assertions.assertFalse(
            noUser.roles().sameAs(noUser.roles())
        );
    }

    @Test
    public void testRoleOfNormalUser() {
        User reqUser = new Requester(
            Long.valueOf(1), "User", "ROLE_ADMIN"
        );

        Assertions.assertTrue(
            reqUser.roles().sameAs(new SimpleRole("ROLE_ADMIN"))
        );

        Assertions.assertFalse(
            reqUser.roles().sameAs(new SimpleRole("ROLE_GUEST"))
        );
    }
}
