package com.filmbooking.booking_service.utils.token.claims;

import com.filmbooking.booking_service.utils.permission.DefaultPermissions;
import com.filmbooking.booking_service.utils.permission.NoPermission;
import com.filmbooking.booking_service.utils.permission.Permissions;
import com.filmbooking.booking_service.utils.user.NullUser;
import com.filmbooking.booking_service.utils.user.Requester;
import com.filmbooking.booking_service.utils.user.User;

import org.json.JSONObject;

public class DefaultClaims implements Claims {

    JSONObject payload;

    public DefaultClaims(JSONObject payload) {
        this.payload = payload;
    }

    @Override
    public Permissions perms() {
        try {
            return new DefaultPermissions(
                this.payload.getJSONObject("permission")
            );
        }
        catch (Exception e) {
            System.out.println("[INFO] Failed to get permissions");
            e.printStackTrace();
            return new NoPermission();
        }
    }

    @Override
    public User requester() {
        try {
            return new Requester(
                this.payload.getLong("id"),
                this.payload.getString("sub"),
                this.payload.getString("roles")
            );
        }
        catch (Exception e) {
            System.out.println("[INFO] Failed to get requester info");
            e.printStackTrace();
            return new NullUser();
        }
    }

}
