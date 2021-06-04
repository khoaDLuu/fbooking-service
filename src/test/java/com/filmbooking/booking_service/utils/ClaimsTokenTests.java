package com.filmbooking.booking_service.utils;

import com.filmbooking.booking_service.utils.permission.Permissions;
import com.filmbooking.booking_service.utils.permission.operation.Operation;
import com.filmbooking.booking_service.utils.permission.operation.SimpleOperation;
import com.filmbooking.booking_service.utils.token.DefaultToken;
import com.filmbooking.booking_service.utils.token.Token;
import com.filmbooking.booking_service.utils.token.ValidatedToken;
import com.filmbooking.booking_service.utils.token.claims.Claims;
import com.filmbooking.booking_service.utils.token.claims.DefaultClaims;
import com.filmbooking.booking_service.utils.user.Requester;
import com.filmbooking.booking_service.utils.user.User;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ClaimsTokenTests {

    @Test
    public void testClaimsReturningPermissions() throws JSONException {
        Claims claims = new DefaultClaims(
            new JSONObject(
                "{\"sub\":\"admin1\"," +
                "\"roles\":\"ROLE_ADMIN\"," +
                "\"id\":39," +
                "\"permission\":" +
                "{\"2\":\"READ\",\"3\":\"UPDATE\",\"4\":\"DELETE\"}," +
                "\"iat\":1622304413," +
                "\"exp\":1622390813}"
            )
        );
        Operation updOp = new SimpleOperation("UPDATE");
        Operation createOp = new SimpleOperation("CREATE");

        Assertions.assertTrue(claims.perms().allow(updOp));
        Assertions.assertFalse(claims.perms().allow(createOp));
    }

    @Test
    public void testClaimsReturningNoPermission() {
        Claims brokenClaims = new DefaultClaims(new JSONObject());
        Operation updOp = new SimpleOperation("UPDATE");

        Assertions.assertFalse(brokenClaims.perms().allow(updOp));
    }

    @Test
    public void testClaimsReturningRequester() throws JSONException {
        Claims claims = new DefaultClaims(
            new JSONObject(
                "{\"sub\":\"admin1\"," +
                "\"roles\":\"ROLE_ADMIN\"," +
                "\"id\":39," +
                "\"permission\":" +
                "{\"2\":\"READ\",\"3\":\"UPDATE\",\"4\":\"DELETE\"}," +
                "\"iat\":1622304413," +
                "\"exp\":1622390813}"
            )
        );
        User dummyAdmin = new Requester(Long.valueOf(2), "Dummy", "ROLE_ADMIN");
        User dummyGuest = new Requester(Long.valueOf(3), "Dumbo", "ROLE_GUEST");

        Assertions.assertTrue(
            claims.requester().roles().sameAs(dummyAdmin.roles())
        );
        Assertions.assertFalse(
            claims.requester().roles().sameAs(dummyGuest.roles())
        );
    }

    @Test
    public void testClaimsReturningNullUser() {
        Claims brokenClaims = new DefaultClaims(new JSONObject());
        User dummyUser = new Requester(Long.valueOf(2), "Dummy", "ROLE_ADMIN");

        Assertions.assertFalse(
            brokenClaims.requester().roles().sameAs(dummyUser.roles())
        );
    }

    @Test
    public void testBrokenAuthToken() {
        Token emptyToken = new DefaultToken("");
        Assertions.assertEquals(
            emptyToken.algo(), "",
            "Empty token doesn't have empty algorithm name."
        );
    }

    @Test
    public void testWellformedAuthToken() {
        Token wellformedToken = new DefaultToken(
            "eyJhbGciOiJIUzI1NiJ9." +
            "eyJzdWIiOiJhZG1pbjEiLCJyb2xlcyI6IlJPTEVfQURNSU4iLCJpZCI6MzksInBl" +
            "cm1pc3Npb24iOnsiMSI6IkNSRUFURSIsIjIiOiJSRUFEIiwiMyI6IlVQREFURSIs" +
            "IjQiOiJERUxFVEUifSwiaWF0IjoxNjIyMzQ3NjEzLCJleHAiOjI2MjI0MzQwMTN9" +
            ".v_bJdLSGi9LRiDaHPVdVE-xSUBk4noU3xcbg5bC2okc"
        );
        Permissions perms = wellformedToken.claims().perms();
        Operation readOp = new SimpleOperation("READ");

        Assertions.assertEquals(
            wellformedToken.algo(), "HS256",
            "Token doesn't contain the correct encryption algorithm name."
        );
        Assertions.assertTrue(
            perms.allow(readOp),
            "Permission list extracted from the token doesn't allow READ."
        );
        Assertions.assertNotEquals(
            wellformedToken.signature(), "",
            "Token has an empty signature."
        );
    }

    @Test
    public void testValidatedAuthToken() {
        Token validToken = new ValidatedToken(new DefaultToken(
            "eyJhbGciOiJIUzI1NiJ9." +
            "eyJzdWIiOiJhZG1pbjEiLCJyb2xlcyI6IlJPTEVfQURNSU4iLCJpZCI6MzksInBl" +
            "cm1pc3Npb24iOnsiMSI6IkNSRUFURSIsIjIiOiJSRUFEIiwiMyI6IlVQREFURSIs" +
            "IjQiOiJERUxFVEUifSwiaWF0IjoxNjIyMzQ3NjEzLCJleHAiOjI2MjI0MzQwMTN9" +
            ".v_bJdLSGi9LRiDaHPVdVE-xSUBk4noU3xcbg5bC2okc"
        ));
        Permissions perms = validToken.claims().perms();
        Operation updOp = new SimpleOperation("UPDATE");

        Assertions.assertNotEquals(
            validToken.algo(), "ES256",
            "Token doesn't contain the correct encryption algorithm name."
        );
        Assertions.assertTrue(
            perms.allow(updOp),
            "Permission list extracted from the token doesn't allow UPDATE."
        );
    }

    @Test
    public void testExpiredAuthToken() {
        Token validToken = new ValidatedToken(new DefaultToken(
            "eyJhbGciOiJIUzI1NiJ9." +
            "eyJzdWIiOiJhZG1pbjEiLCJyb2xlcyI6IlJPTEVfQURNSU4iLCJpZCI6MzksInBl" +
            "cm1pc3Npb24iOnsiMSI6IkNSRUFURSIsIjIiOiJSRUFEIiwiMyI6IlVQREFURSIs" +
            "IjQiOiJERUxFVEUifSwiaWF0IjoxNjIxODM4MjE0LCJleHAiOjE2MjE5MjQ2MTR9" +
            ".lGRE9Em0XnNiHk_fLn9B-rOFgzx3if1v7iHti2aXOy4"
        ));

        Assertions.assertTrue(
            validToken.expired(),
            "Token whose expiry date has passed is not expired."
        );
    }

}
