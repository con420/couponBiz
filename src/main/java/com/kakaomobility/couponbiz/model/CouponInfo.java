package com.kakaomobility.couponbiz.model;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouponInfo {
    private Long couponInfoId;
    private DiscountType discountType;
    private int discountValue;
    private LocalDateTime createdAt;
    private LocalDateTime expiredAt;
    private LocalDateTime updatedAt;

    public int getDiscountAmount(int orderAmount) {
        if (discountType == DiscountType.FIXED) {
            return Math.min(orderAmount, discountValue);
        }
        // TODO: RATE 타입 구현
        return 0;
    }
}
