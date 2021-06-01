package com.filmbooking.booking_service.utils;

import com.filmbooking.booking_service.utils.permission.DefaultPermissions;
import com.filmbooking.booking_service.utils.permission.NoPermission;
import com.filmbooking.booking_service.utils.permission.Permissions;
import com.filmbooking.booking_service.utils.permission.operation.Operation;
import com.filmbooking.booking_service.utils.permission.operation.SimpleOperation;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.json.JSONException;
import org.json.JSONObject;

public class OpPermsTests {

    @Test
    public void testOperationEquality() {
        Assertions.assertEquals(
            new SimpleOperation("DELETE"),
            new SimpleOperation("DELETE")
        );
    }

    @Test
    public void testOperationAllowedByPermissions() throws JSONException {
        Permissions perms = new DefaultPermissions(
            new JSONObject(
                "{\"1\":\"CREATE\",\"2\":\"DELETE\",\"3\":\"UPDATE\"}"
            )
        );
        Operation delOp = new SimpleOperation("DELETE");
        Operation readOp = new SimpleOperation("READ");

        Assertions.assertTrue(perms.allow(delOp));
        Assertions.assertFalse(perms.allow(readOp));
    }

    @Test
    public void testOperationForbiddenByPermissions() throws JSONException {
        Permissions perms = new DefaultPermissions(
            new JSONObject(
                "{\"1\":\"CREATE\",\"2\":\"DELETE\",\"3\":\"UPDATE\"}"
            )
        );
        Operation bkCrOp = new SimpleOperation("BOOKING.CREATE");
        Operation updOp = new SimpleOperation("UPDATE");

        Assertions.assertTrue(perms.forbid(bkCrOp));
        Assertions.assertFalse(perms.forbid(updOp));
    }

    @Test
    public void testOperationNotAllowedByEmptyPermissions()
            throws JSONException {
        Permissions perms = new NoPermission();
        Operation delOp = new SimpleOperation("DELETE");
        Assertions.assertFalse(perms.allow(delOp));
    }

}
