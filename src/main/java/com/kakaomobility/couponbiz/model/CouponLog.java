package com.kakaomobility.couponbiz.model;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouponLog {
    private Long couponLogId;
    private Coupon coupon;
    private int discountApplied;
    private LocalDateTime usedAt;
    private LocalDateTime cancelledAt;
}
