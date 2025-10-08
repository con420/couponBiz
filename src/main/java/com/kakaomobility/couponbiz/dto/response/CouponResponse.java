package com.kakaomobility.couponbiz.dto.response;

import com.kakaomobility.couponbiz.model.Coupon;
import lombok.Getter;
import lombok.Builder;
import java.time.LocalDateTime;

@Getter
@Builder
public class CouponResponse {
    private Long couponId;
    private Long userId;
    private String couponCode;
    private LocalDateTime issuedAt;
    private String status;
    private String type;
    private CouponInfoResponse couponInfo;

    public static CouponResponse from(Coupon coupon) {
        return CouponResponse.builder()
                .couponId(coupon.getCouponId())
                .userId(coupon.getUserId())
                .couponCode(coupon.getCouponCode())
                .issuedAt(coupon.getIssuedAt())
                .status(coupon.getStatus().name())
                .type(coupon.getType().name())
                .couponInfo(CouponInfoResponse.from(coupon.getCouponInfo()))
                .build();
    }
}
