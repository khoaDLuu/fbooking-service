package com.filmbooking.booking_service.utils.token;

import java.time.Instant;
import java.util.Base64;

import com.filmbooking.booking_service.utils.token.claims.Claims;
import com.filmbooking.booking_service.utils.token.claims.DefaultClaims;
import com.filmbooking.booking_service.utils.token.claims.EmptyClaims;

import org.json.JSONException;
import org.json.JSONObject;

public class DefaultToken implements Token {
    String raw;

    public DefaultToken(String raw) {
        this.raw = raw;
    }

    @Override
    public String algo() {
        try {
            JSONObject jwtHeader = new JSONObject(new String(
                Base64.getDecoder().decode(this.parts()[0])
            ));
            return jwtHeader.getString("alg");
        }
        catch (
            IndexOutOfBoundsException |
            JSONException |
            IllegalArgumentException e
        ) {
            System.out.println("[INFO] Failed to extract token algo");
            e.printStackTrace();
            return "";
        }
    }

    @Override
    public Claims claims() {
        try {
            String jwtPayload = this.parts()[1];
            return new DefaultClaims(new JSONObject(new String(
                Base64.getDecoder().decode(jwtPayload)
            )));
        }
        catch (
            IndexOutOfBoundsException |
            JSONException |
            IllegalArgumentException e
        ) {
            System.out.println("[INFO] Failed to extract claims from token");
            e.printStackTrace();
            return new EmptyClaims();
        }
    }

    @Override
    public String signature() {
        try {
            return this.parts()[2];
        }
        catch (
            IndexOutOfBoundsException |
            JSONException |
            IllegalArgumentException e
        ) {
            System.out.println("[DEBUG] Failed to extract token signature");
            e.printStackTrace();
            return "";
        }
    }

    @Override
    public boolean expired() {
        try {
            String jwtPayload = this.parts()[1];
            Instant expiration = Instant.ofEpochSecond(
                new JSONObject(new String(
                    Base64.getDecoder().decode(jwtPayload)
                )).getLong("exp")
            );
            return Instant.now().isAfter(expiration);
        }
        catch (
            IndexOutOfBoundsException |
            JSONException |
            IllegalArgumentException e
        ) {
            System.out.println("[DEBUG] Failed to check token expiration");
            e.printStackTrace();
            return true;
        }
    }

    private String[] parts() {
        return raw.split("\\.");
    }

}
