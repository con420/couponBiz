package com.kakaomobility.couponbiz.model;

import com.kakaomobility.couponbiz.exception.CouponAlreadyUsedException;
import com.kakaomobility.couponbiz.exception.CouponExpiredException;
import com.kakaomobility.couponbiz.exception.CouponInvalidStateException;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Coupon {
    private Long couponId;
    private CouponInfo couponInfo;
    private Long userId;
    private String couponCode;
    private LocalDateTime issuedAt;
    private CouponStatus status;
    private CouponType type;

    public boolean isUsable() {
        return status == CouponStatus.ISSUED
                && type == CouponType.NORMAL
                && couponInfo.getExpiredAt().isAfter(LocalDateTime.now());
    }

    public void use() {
        if (this.status == CouponStatus.USED) {
            throw new CouponAlreadyUsedException("이미 사용된 쿠폰입니다.");
        }
        if (couponInfo.getExpiredAt().isBefore(LocalDateTime.now())) {
            throw new CouponExpiredException("만료된 쿠폰입니다.");
        }
        if (!isUsable()) {
            throw new CouponInvalidStateException("사용할 수 없는 상태의 쿠폰입니다.");
        }
        this.status = CouponStatus.USED;
    }

    public void expire() {
        if (this.status != CouponStatus.ISSUED) {
            throw new CouponInvalidStateException("만료 처리할 수 없는 상태의 쿠폰입니다.");
        }
        this.status = CouponStatus.CANCEL;
    }
}