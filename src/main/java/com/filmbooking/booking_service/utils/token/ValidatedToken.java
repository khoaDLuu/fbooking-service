package com.filmbooking.booking_service.utils.token;

import com.filmbooking.booking_service.utils.token.claims.Claims;
import com.filmbooking.booking_service.utils.token.claims.EmptyClaims;

public class ValidatedToken implements Token {

    private Token original;

    public ValidatedToken(Token orig) {
        this.original = orig;
    }

    @Override
    public String algo() {
        return this.original.algo();
    }

    @Override
    public Claims claims() {
        final boolean valid = true; // NOTE: check validity here
        if (this.expired() || !valid) {
            System.out.println("[INFO] Token expired or invalid");
            return new EmptyClaims();
        }
        else {
            return this.original.claims();
        }
    }

    @Override
    public String signature() {
        return this.original.signature();
    }

    @Override
    public boolean expired() {
        return this.original.expired();
    }

}
