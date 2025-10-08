package com.kakaomobility.couponbiz.dto.request;

import lombok.Getter;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponInfoCreateRequest {
    private String discountType; // "FIXED" or "RATE"
    private int discountValue;
    private LocalDateTime expiredAt;
}
