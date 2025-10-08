package com.kakaomobility.couponbiz.dto.response;

import com.kakaomobility.couponbiz.model.CouponLog;
import lombok.Getter;
import lombok.Builder;
import java.time.LocalDateTime;

@Getter
@Builder
public class CouponLogResponse {
    private Long couponLogId;
    private int discountApplied;
    private LocalDateTime usedAt;
    private LocalDateTime cancelledAt;
    private CouponResponse coupon;

    public static CouponLogResponse from(CouponLog couponLog) {
        return CouponLogResponse.builder()
                .couponLogId(couponLog.getCouponLogId())
                .discountApplied(couponLog.getDiscountApplied())
                .usedAt(couponLog.getUsedAt())
                .cancelledAt(couponLog.getCancelledAt())
                .coupon(CouponResponse.from(couponLog.getCoupon()))
                .build();
    }
}
