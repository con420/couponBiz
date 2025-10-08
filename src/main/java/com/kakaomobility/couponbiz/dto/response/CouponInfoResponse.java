package com.kakaomobility.couponbiz.dto.response;

import com.kakaomobility.couponbiz.model.CouponInfo;
import lombok.Getter;
import lombok.Builder;
import java.time.LocalDateTime;

@Getter
@Builder
public class CouponInfoResponse {
    private Long couponInfoId;
    private String discountType;
    private int discountValue;
    private LocalDateTime createdAt;
    private LocalDateTime expiredAt;
    private LocalDateTime updatedAt;

    public static CouponInfoResponse from(CouponInfo couponInfo) {
        return CouponInfoResponse.builder()
                .couponInfoId(couponInfo.getCouponInfoId())
                .discountType(couponInfo.getDiscountType().name())
                .discountValue(couponInfo.getDiscountValue())
                .createdAt(couponInfo.getCreatedAt())
                .expiredAt(couponInfo.getExpiredAt())
                .updatedAt(couponInfo.getUpdatedAt())
                .build();
    }
}
