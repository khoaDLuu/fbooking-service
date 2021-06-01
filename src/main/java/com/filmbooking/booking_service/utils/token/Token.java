package com.filmbooking.booking_service.utils.token;

import com.filmbooking.booking_service.utils.token.claims.Claims;

public interface Token {
    public String algo();
    public Claims claims();
    public String signature();
    public boolean expired();
}
