package com.kakaomobility.couponbiz.exception;

public class CouponAlreadyUsedException extends RuntimeException {
    public CouponAlreadyUsedException(String message) {
        super(message);
    }
}