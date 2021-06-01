package com.filmbooking.booking_service.utils.authHeader;

import com.filmbooking.booking_service.utils.token.Token;

public interface AuthHeader {
    public String type();
    public Token token();
    public boolean wellformed();
}
