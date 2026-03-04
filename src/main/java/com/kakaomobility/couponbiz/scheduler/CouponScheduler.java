package com.kakaomobility.couponbiz.scheduler;

import com.kakaomobility.couponbiz.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CouponScheduler {
    private final CouponService couponService;

    /**
     * 만료된 쿠폰 자동 처리
     * Cron 표현식: 0 */1 * * * *
     * - 초: 0초
     * - 분: 매 1분마다 (*/1)
     * - 시간: 모든 시간 (*)
     * - 날짜: 모든 날짜 (*)
     * - 월: 모든 월 (*)
     * - 요일: 모든 요일 (*)
     *
     * 결과: 매 1분마다 실행됨
     */
    @Scheduled(cron = "0 */1 * * * *")  // ✅ 공백 제거됨
    public void processCouponExpiration() {
        couponService.expireCoupons();
    }
}
