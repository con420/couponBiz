package com.kakaomobility.couponbiz.service.impl;

import com.kakaomobility.couponbiz.exception.CouponAccessDeniedException;
import com.kakaomobility.couponbiz.exception.CouponAlreadyUsedException;
import com.kakaomobility.couponbiz.exception.CouponNotFoundException;
import com.kakaomobility.couponbiz.mapper.CouponInfoMapper;
import com.kakaomobility.couponbiz.mapper.CouponLogMapper;
import com.kakaomobility.couponbiz.mapper.CouponMapper;
import com.kakaomobility.couponbiz.model.*;
import com.kakaomobility.couponbiz.service.CouponService;
import com.kakaomobility.couponbiz.util.CouponCodeGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CouponServiceImpl implements CouponService {
    private final CouponInfoMapper couponInfoMapper;
    private final CouponMapper couponMapper;
    private final CouponLogMapper couponLogMapper;

    @Override
    @Transactional
    public CouponInfo createCouponInfo(String discountTypeStr, int discountValue, LocalDateTime expiredAt) {
        DiscountType discountType = DiscountType.valueOf(discountTypeStr);
        // TODO: 할인 타입에 따른 유효성 검증 추가

        CouponInfo couponInfo = CouponInfo.builder()
                .discountType(discountType)
                .discountValue(discountValue)
                .createdAt(LocalDateTime.now())
                .expiredAt(expiredAt)
                .updatedAt(LocalDateTime.now())
                .build();

        couponInfoMapper.insert(couponInfo);
        return couponInfo;
    }

    @Override
    @Transactional
    public Coupon issueCoupon(Long couponInfoId, Long userId) {
        CouponInfo info = couponInfoMapper.selectById(couponInfoId);
        if (info == null) {
            throw new CouponNotFoundException("존재하지 않는 쿠폰 정보입니다. id=" + couponInfoId);
        }

        String couponCode = CouponCodeGenerator.generate(12);

        Coupon coupon = Coupon.builder()
                .couponInfo(info)
                .userId(userId)
                .couponCode(couponCode)
                .issuedAt(LocalDateTime.now())
                .status(CouponStatus.ISSUED)
                .type(CouponType.NORMAL)
                .build();

        couponMapper.insert(coupon);
        return coupon;
    }

    @Override
    public Coupon getCoupon(String couponCode) {
        Coupon coupon = couponMapper.selectByCouponCode(couponCode);
        if (coupon == null) {
            throw new CouponNotFoundException("존재하지 않는 쿠폰입니다. code=" + couponCode);
        }
        return coupon;
    }

    @Override
    @Transactional
    public CouponLog useCoupon(String couponCode, Long userId, int orderAmount) {
        Coupon coupon = couponMapper.selectByCouponCode(couponCode);
        if (coupon == null) {
            throw new CouponNotFoundException("존재하지 않는 쿠폰입니다");
        }
        if (!coupon.getUserId().equals(userId)) {
            throw new CouponAccessDeniedException("쿠폰 소유자가 아닙니다.");
        }
        if (coupon.getStatus() == CouponStatus.USED) {
            throw new CouponAlreadyUsedException("이미 사용된 쿠폰입니다.");
        }

        coupon.use();
        couponMapper.updateStatus(coupon);

        // 할인 금액 계산 및 로그 생성
        int discount = coupon.getCouponInfo().getDiscountAmount(orderAmount);
        CouponLog log = CouponLog.builder()
                .coupon(coupon)
                .discountApplied(discount)
                .usedAt(LocalDateTime.now())
                .build();

        couponLogMapper.insert(log);
        return log;
    }

    @Override
    public List<Coupon> getUserCoupons(Long userId, CouponStatus status) {
        if (status == null) {
            return couponMapper.selectByUserId(userId);
        }
        return couponMapper.selectByUserIdAndStatus(userId, status);
    }

    @Override
    @Transactional
    public void expireCoupons() {
        List<Coupon> expiredCoupons = couponMapper.selectExpiredCoupons(LocalDateTime.now());

        for (Coupon coupon : expiredCoupons) {
            coupon.expire();
            // TODO : 쿠폰 만료 실패 시 알람처리
            couponMapper.updateStatus(coupon);
        }
    }
}