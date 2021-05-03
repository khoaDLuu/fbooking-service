package com.filmbooking.booking_service;

public class PaypalResponse {
    private String orderId;
    private String charge;
    private String createTime;

    public PaypalResponse(String orderId, String charge, String createTime) {
        this.orderId = orderId;
        this.charge = charge;
        this.createTime = createTime;
    }

    public String getOrderId() {
        return this.orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getCharge() {
        return this.charge;
    }

    public void setCharge(String charge) {
        this.charge = charge;
    }

    public String getCreateTime() {
        return this.createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }
}
