package com.kakaomobility.couponbiz.service;

import com.kakaomobility.couponbiz.model.*;
import java.time.LocalDateTime;
import java.util.List;

public interface CouponService {
    /**
     * 쿠폰 정보 생성
     * @param discountType "FIXED", "RATE"
     * @param discountValue 할인 금액 또는 할인율
     * @param expiredAt 만료일시
     * @return 생성된 CouponInfo 객체
     */
    CouponInfo createCouponInfo(String discountType, int discountValue, LocalDateTime expiredAt);

    /**
     * 쿠폰 발급
     * @param couponInfoId 쿠폰 정보 ID
     * @param userId 사용자 ID
     * @return 발급한 Coupon
     */
    Coupon issueCoupon(Long couponInfoId, Long userId);

    /**
     * 쿠폰 조회
     * @param couponCode 쿠폰 코드
     * @return 조회한 Coupon
     */
    Coupon getCoupon(String couponCode);

    /**
     * 쿠폰 사용
     * @param couponCode 쿠폰 코드
     * @param userId 사용자 ID
     * @param originalAmount 원래 결제 금액
     * @return 쿠폰 사용 로그
     */
    CouponLog useCoupon(String couponCode, Long userId, int originalAmount);

    /**
     * 사용자 쿠폰 리스트 조회
     * @param userId 사용자 ID
     * @param status 조회할 쿠폰 타입(CouponStatus)
     * @return 쿠폰 리스트
     */
    List<Coupon> getUserCoupons(Long userId, CouponStatus status);

    /**
     * 만료된 쿠폰 상태를 EXPIRED로 변경
     */
    void expireCoupons();
}
