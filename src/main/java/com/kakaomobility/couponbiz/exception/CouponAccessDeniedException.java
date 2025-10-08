package com.kakaomobility.couponbiz.exception;

public class CouponAccessDeniedException extends RuntimeException {
    public CouponAccessDeniedException(String message) {
        super(message);
    }
}