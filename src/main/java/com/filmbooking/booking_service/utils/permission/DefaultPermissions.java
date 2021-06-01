package com.filmbooking.booking_service.utils.permission;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.filmbooking.booking_service.utils.permission.operation.Operation;
import com.filmbooking.booking_service.utils.permission.operation.SimpleOperation;

import org.json.JSONObject;

public class DefaultPermissions implements Permissions {

    JSONObject rawPerms;

    public DefaultPermissions(JSONObject raw) {
        this.rawPerms = raw;
    }

    @Override
    public boolean allow(Operation op) {
        Iterable<String> keysIterable = () -> this.rawPerms.keys();
        Set<SimpleOperation> permittedOps = StreamSupport.stream(
                keysIterable.spliterator(), false
            )
            .map(key -> new SimpleOperation(this.rawPerms.getString(key)))
            .collect(Collectors.toSet());

        return permittedOps.contains(op);
    }

    @Override
    public boolean forbid(Operation op) {
        return !this.allow(op);
    }

}
