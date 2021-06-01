package com.filmbooking.booking_service.utils;

import com.filmbooking.booking_service.utils.authHeader.AuthHeader;
import com.filmbooking.booking_service.utils.authHeader.DefaultAuthHeader;
import com.filmbooking.booking_service.utils.permission.operation.Operation;
import com.filmbooking.booking_service.utils.permission.operation.SimpleOperation;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

public class AuthHeaderTests {

    @Test
    public void testAuthHeaderWithNoType() {
        HttpHeaders headers = new HttpHeaders();
        headers.add(
            "Authorization",
            "eyJhbGciOiJIUzI1NiJ9." +
            "eyJzdWIiOiJhZG1pbjEiLCJyb2xlcyI6IlJPTEVfQURNSU4iLCJpZCI6MzksInBl" +
            "cm1pc3Npb24iOnsiMSI6IkNSRUFURSIsIjIiOiJSRUFEIiwiMyI6IlVQREFURSIs" +
            "IjQiOiJERUxFVEUifSwiaWF0IjoxNjIyMzQ3NjEzLCJleHAiOjI2MjI0MzQwMTN9" +
            ".v_bJdLSGi9LRiDaHPVdVE-xSUBk4noU3xcbg5bC2okc"
        );
        AuthHeader authHeader = new DefaultAuthHeader(headers);

        Assertions.assertEquals(authHeader.type(), "");
    }

    @Test
    public void testAuthHeaderWithWrongType() {
        HttpHeaders headers = new HttpHeaders();
        headers.add(
            "Authorization",
            "Basic " +
            "eyJhbGciOiJIUzI1NiJ9." +
            "eyJzdWIiOiJhZG1pbjEiLCJyb2xlcyI6IlJPTEVfQURNSU4iLCJpZCI6MzksInBl" +
            "cm1pc3Npb24iOnsiMSI6IkNSRUFURSIsIjIiOiJSRUFEIiwiMyI6IlVQREFURSIs" +
            "IjQiOiJERUxFVEUifSwiaWF0IjoxNjIyMzQ3NjEzLCJleHAiOjI2MjI0MzQwMTN9" +
            ".v_bJdLSGi9LRiDaHPVdVE-xSUBk4noU3xcbg5bC2okc"
        );
        AuthHeader authHeader = new DefaultAuthHeader(headers);

        Assertions.assertNotEquals(authHeader.type(), "Bearer");
    }

    @Test
    public void testMalformedAuthHeader() {
        HttpHeaders headers = new HttpHeaders();
        headers.add(
            "Authorization",
            "Bearer  " +
            "eyJhbGciOiJIUzI1NiJ9." +
            "eyJzdWIiOiJhZG1pbjEiLCJyb2xlcyI6IlJPTEVfQURNSU4iLCJpZCI6MzksInBl" +
            "cm1pc3Npb24iOnsiMSI6IkNSRUFURSIsIjIiOiJSRUFEIiwiMyI6IlVQREFURSIs" +
            "IjQiOiJERUxFVEUifSwiaWF0IjoxNjIyMzQ3NjEzLCJleHAiOjI2MjI0MzQwMTN9" +
            ".v_bJdLSGi9LRiDaHPVdVE-xSUBk4noU3xcbg5bC2okc"
        );
        AuthHeader authHeader = new DefaultAuthHeader(headers);

        Assertions.assertFalse(authHeader.wellformed());
    }

    @Test
    public void testOkAuthHeader() {
        HttpHeaders headers = new HttpHeaders();
        headers.add(
            "Authorization",
            "Bearer " +
            "eyJhbGciOiJIUzI1NiJ9." +
            "eyJzdWIiOiJhZG1pbjEiLCJyb2xlcyI6IlJPTEVfQURNSU4iLCJpZCI6MzksInBl" +
            "cm1pc3Npb24iOnsiMSI6IkNSRUFURSIsIjIiOiJSRUFEIiwiMyI6IlVQREFURSIs" +
            "IjQiOiJERUxFVEUifSwiaWF0IjoxNjIyMzQ3NjEzLCJleHAiOjI2MjI0MzQwMTN9" +
            ".v_bJdLSGi9LRiDaHPVdVE-xSUBk4noU3xcbg5bC2okc"
        );
        AuthHeader authHeader = new DefaultAuthHeader(headers);
        Operation dummyOp = new SimpleOperation("DUMMY");

        Assertions.assertFalse(
            authHeader.token().claims().perms().allow(dummyOp)
        );
    }

    @Test
    public void testBadAuthHeader() {
        HttpHeaders headers = new HttpHeaders();
        headers.add(
            "Authorization",
            "Bearer " +
            "eyJzdWIiOiJhZG1pbjEiLCJyb2xlcyI6IlJPTEVfQURNSU4iLCJpZCI6MzksInBl" +
            "cm1pc3Npb24iOnsiMSI6IkNSRUFURSIsIjIiOiJSRUFEIiwiMyI6IlVQREFURSIs" +
            "IjQiOiJERUxFVEUifSwiaWF0IjoxNjIyMzQ3NjEzLCJleHAiOjE2MjI0MzQwMTN9" +
            ".HxJqGKLwKuV59IquKW4u0QwN-9raXBjLcufFCMytLE4"
        );
        AuthHeader authHeader = new DefaultAuthHeader(headers);
        Operation dummyOp = new SimpleOperation("DUMMY");

        Assertions.assertFalse(
            authHeader.token().claims().perms().allow(dummyOp)
        );
    }

}
