package com.kakaomobility.couponbiz.util;

public class CouponCodeGenerator {
    private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    private CouponCodeGenerator() {}

    public static String generate(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int idx = (int) (Math.random() * CHARS.length());
            sb.append(CHARS.charAt(idx));
        }
        return sb.toString();
    }
}

