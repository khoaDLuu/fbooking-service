package com.filmbooking.booking_service.utils.authHeader;

import com.filmbooking.booking_service.utils.token.DefaultToken;
import com.filmbooking.booking_service.utils.token.NullToken;
import com.filmbooking.booking_service.utils.token.Token;

import org.springframework.http.HttpHeaders;

public class DefaultAuthHeader implements AuthHeader {

    private HttpHeaders headers;

    public DefaultAuthHeader(HttpHeaders headers) {
        this.headers = headers;
    }

    @Override
    public String type() {
        try {
            if (!this.wellformed()) {
                return "";
            }
            return this.headers
                .getOrEmpty("Authorization").get(0)
                .split(" ")[0];
        }
        catch (ArrayIndexOutOfBoundsException e) {
            return "";
        }
    }

    @Override
    public Token token() {
        try {
            if (!this.wellformed()) {
                System.out.println("[INFO] Malformed auth header");
                return new NullToken();
            }
            String raw = this.headers
                .getOrEmpty("Authorization").get(0)
                .split(" ")[1];
            if (raw.strip().equals("")) {
                System.out.println("[INFO] Empty auth header");
                return new NullToken();
            }
            else {
                return new DefaultToken(raw);
            }
        }
        catch (IndexOutOfBoundsException e) {
            System.out.println("[INFO] Malformed auth header");
            e.printStackTrace();
            return new NullToken();
        }
    }

    @Override
    public boolean wellformed() {
        return this.headers
            .getOrEmpty("Authorization").get(0)
            .split(" ").length == 2;
    }

}
