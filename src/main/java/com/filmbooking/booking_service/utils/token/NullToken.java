package com.filmbooking.booking_service.utils.token;

import com.filmbooking.booking_service.utils.token.claims.Claims;
import com.filmbooking.booking_service.utils.token.claims.EmptyClaims;

public class NullToken implements Token {

    @Override
    public String algo() {
        return "";
    }

    @Override
    public Claims claims() {
        return new EmptyClaims();
    }

    @Override
    public String signature() {
        return "";
    }

    @Override
    public boolean expired() {
        return false;
    }

}
