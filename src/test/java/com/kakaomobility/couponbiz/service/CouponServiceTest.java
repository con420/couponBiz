package com.kakaomobility.couponbiz.service;

import com.kakaomobility.couponbiz.exception.CouponNotFoundException;
import com.kakaomobility.couponbiz.model.*;
import com.kakaomobility.couponbiz.mapper.CouponInfoMapper;
import com.kakaomobility.couponbiz.mapper.CouponLogMapper;
import com.kakaomobility.couponbiz.mapper.CouponMapper;
import com.kakaomobility.couponbiz.service.impl.CouponServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * CouponServiceImpl의 기본 기능 테스트
 *
 * 테스트 대상:
 * 1. 쿠폰 정보 생성 (createCouponInfo)
 * 2. 쿠폰 발급 (issueCoupon)
 * 3. 쿠폰 조회 (getCoupon)
 * 4. 쿠폰 사용 (useCoupon)
 * 5. 사용자 쿠폰 목록 조회 (getUserCoupons)
 * 6. 만료 쿠폰 처리 (expireCoupons)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("쿠폰 서비스 기능 테스트")
class CouponServiceTest {

    @Mock
    private CouponInfoMapper couponInfoMapper;

    @Mock
    private CouponMapper couponMapper;

    @Mock
    private CouponLogMapper couponLogMapper;

    @InjectMocks
    private CouponServiceImpl couponService;

    private CouponInfo testCouponInfo;
    private Coupon testCoupon;

    @BeforeEach
    void setUp() {
        testCouponInfo = CouponInfo.builder()
                .couponInfoId(1L)
                .discountType(DiscountType.FIXED)
                .discountValue(10000)
                .createdAt(LocalDateTime.now())
                .expiredAt(LocalDateTime.now().plusDays(30))
                .updatedAt(LocalDateTime.now())
                .build();

        testCoupon = Coupon.builder()
                .couponId(1L)
                .couponInfo(testCouponInfo)
                .userId(1L)
                .couponCode("ABC123456789")
                .issuedAt(LocalDateTime.now())
                .status(CouponStatus.ISSUED)
                .type(CouponType.NORMAL)
                .build();
    }

    @Test
    @DisplayName("쿠폰 정보 생성 성공")
    void testCreateCouponInfo() {
        // Given
        String discountType = "FIXED";
        int discountValue = 10000;
        LocalDateTime expiredAt = LocalDateTime.now().plusDays(30);

        // When
        CouponInfo result = couponService.createCouponInfo(discountType, discountValue, expiredAt);

        // Then
        assertNotNull(result);
        assertEquals(DiscountType.FIXED, result.getDiscountType());
        assertEquals(10000, result.getDiscountValue());
        assertEquals(expiredAt, result.getExpiredAt());
        verify(couponInfoMapper, times(1)).insert(any(CouponInfo.class));
    }

    @Test
    @DisplayName("쿠폰 발급 성공")
    void testIssueCoupon() {
        // Given
        Long couponInfoId = 1L;
        Long userId = 1L;

        when(couponInfoMapper.selectById(couponInfoId)).thenReturn(testCouponInfo);
        when(couponMapper.selectByCouponCode(anyString())).thenReturn(testCoupon);

        // When
        Coupon result = couponService.issueCoupon(couponInfoId, userId);

        // Then
        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertEquals(CouponStatus.ISSUED, result.getStatus());
        assertEquals(CouponType.NORMAL, result.getType());
        verify(couponMapper, times(1)).insert(any(Coupon.class));
    }

    @Test
    @DisplayName("존재하지 않는 쿠폰 정보로 발급할 때 예외 발생")
    void testIssueCouponInfoNotFound() {
        // Given
        when(couponInfoMapper.selectById(anyLong())).thenReturn(null);

        // When & Then
        assertThrows(CouponNotFoundException.class, () ->
                couponService.issueCoupon(999L, 1L)
        );
    }

    @Test
    @DisplayName("쿠폰 조회 성공")
    void testGetCoupon() {
        // Given
        when(couponMapper.selectByCouponCode(anyString())).thenReturn(testCoupon);

        // When
        Coupon result = couponService.getCoupon("ABC123456789");

        // Then
        assertNotNull(result);
        assertEquals("ABC123456789", result.getCouponCode());
        assertEquals(1L, result.getCouponId());
    }

    @Test
    @DisplayName("존재하지 않는 쿠폰 조회할 때 예외 발생")
    void testGetCouponNotFound() {
        // Given
        when(couponMapper.selectByCouponCode(anyString())).thenReturn(null);

        // When & Then
        assertThrows(CouponNotFoundException.class, () ->
                couponService.getCoupon("INVALID_CODE")
        );
    }

    @Test
    @DisplayName("사용자별 전체 쿠폰 조회")
    void testGetUserCouponsAll() {
        // Given
        Long userId = 1L;
        List<Coupon> coupons = Arrays.asList(testCoupon, testCoupon);
        when(couponMapper.selectByUserId(userId)).thenReturn(coupons);

        // When
        List<Coupon> result = couponService.getUserCoupons(userId, null);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(couponMapper, times(1)).selectByUserId(userId);
    }

    @Test
    @DisplayName("사용자별 특정 상태의 쿠폰 조회")
    void testGetUserCouponsByStatus() {
        // Given
        Long userId = 1L;
        CouponStatus status = CouponStatus.ISSUED;
        List<Coupon> coupons = Arrays.asList(testCoupon);
        when(couponMapper.selectByUserIdAndStatus(userId, status)).thenReturn(coupons);

        // When
        List<Coupon> result = couponService.getUserCoupons(userId, status);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(couponMapper, times(1)).selectByUserIdAndStatus(userId, status);
    }

    @Test
    @DisplayName("쿠폰 사용 성공")
    void testUseCoupon() {
        // Given
        when(couponMapper.selectByCouponCodeForUpdate(anyString())).thenReturn(testCoupon);
        when(couponLogMapper.insert(any())).thenReturn(1);

        // When
        CouponLog result = couponService.useCoupon("ABC123456789", 1L, 20000);

        // Then
        assertNotNull(result);
        assertEquals(10000, result.getDiscountApplied()); // FIXED 타입, 10000원 할인
        verify(couponMapper, times(1)).updateStatus(any(Coupon.class));
        verify(couponLogMapper, times(1)).insert(any(CouponLog.class));
    }

    @Test
    @DisplayName("할인금액이 주문금액을 초과하면 주문금액만 할인")
    void testUseCouponDiscountExceedsOrderAmount() {
        // Given
        when(couponMapper.selectByCouponCodeForUpdate(anyString())).thenReturn(testCoupon);
        when(couponLogMapper.insert(any())).thenReturn(1);

        // When: 주문금액이 5000원인 경우
        CouponLog result = couponService.useCoupon("ABC123456789", 1L, 5000);

        // Then: 5000원만 할인 (할인값이 10000이지만 주문금액이 5000)
        assertEquals(5000, result.getDiscountApplied());
    }

    @Test
    @DisplayName("만료된 쿠폰 자동 처리")
    void testExpireCoupons() {
        // Given
        Coupon expiredCoupon = Coupon.builder()
                .couponId(1L)
                .couponInfo(testCouponInfo)
                .userId(1L)
                .couponCode("EXPIRED123456")
                .issuedAt(LocalDateTime.now().minusDays(50))
                .status(CouponStatus.ISSUED)
                .type(CouponType.NORMAL)
                .build();

        List<Coupon> expiredCoupons = Arrays.asList(expiredCoupon);
        when(couponMapper.selectExpiredCoupons(any(LocalDateTime.class))).thenReturn(expiredCoupons);

        // When
        couponService.expireCoupons();

        // Then
        assertEquals(CouponStatus.CANCEL, expiredCoupon.getStatus(), "쿠폰 상태가 CANCEL로 변경되어야 함");
        verify(couponMapper, times(1)).updateStatus(any(Coupon.class));
        verify(couponLogMapper, times(1)).insert(any(CouponLog.class));
    }

    @Test
    @DisplayName("만료된 쿠폰이 없으면 아무것도 처리하지 않음")
    void testExpireCouponsEmpty() {
        // Given
        when(couponMapper.selectExpiredCoupons(any(LocalDateTime.class))).thenReturn(Arrays.asList());

        // When
        couponService.expireCoupons();

        // Then
        verify(couponMapper, never()).updateStatus(any(Coupon.class));
        verify(couponLogMapper, never()).insert(any(CouponLog.class));
    }
}
