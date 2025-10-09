package com.kakaomobility.couponbiz.scheduler;

import com.kakaomobility.couponbiz.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CouponScheduler {
    private final CouponService couponService;

    @Scheduled(cron = " 0 */1 * * * *")
    public void processCouponExpiration() {
        couponService.expireCoupons();
    }
}
