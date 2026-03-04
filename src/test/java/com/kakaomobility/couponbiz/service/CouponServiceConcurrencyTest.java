package com.kakaomobility.couponbiz.service;

import com.kakaomobility.couponbiz.exception.CouponAlreadyUsedException;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * CouponServiceImpl의 동시성 테스트
 *
 * 목표: Race Condition이 발생하지 않는지 확인
 * - Pessimistic Locking (비관적 잠금)이 제대로 작동하는지 검증
 * - 동시에 같은 쿠폰을 여러 사용자가 사용하려 할 때 중복 사용이 불가능한지 확인
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("쿠폰 서비스 동시성 테스트")
class CouponServiceConcurrencyTest {

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
        // 테스트용 쿠폰 정보 생성
        testCouponInfo = CouponInfo.builder()
                .couponInfoId(1L)
                .discountType(DiscountType.FIXED)
                .discountValue(10000)
                .createdAt(LocalDateTime.now())
                .expiredAt(LocalDateTime.now().plusDays(30))
                .updatedAt(LocalDateTime.now())
                .build();

        // 테스트용 쿠폰 생성
        testCoupon = Coupon.builder()
                .couponId(1L)
                .couponInfo(testCouponInfo)
                .userId(1L)
                .couponCode("TEST123456789")
                .issuedAt(LocalDateTime.now())
                .status(CouponStatus.ISSUED)
                .type(CouponType.NORMAL)
                .build();
    }

    @Test
    @DisplayName("동시에 같은 쿠폰을 2명이 사용하려 할 때 첫 번째만 성공하고 두 번째는 실패해야 함")
    void testConcurrentCouponUsage() throws InterruptedException {
        // Given
        when(couponMapper.selectByCouponCodeForUpdate(anyString())).thenReturn(testCoupon);
        when(couponLogMapper.insert(any())).thenReturn(1);

        ExecutorService executorService = Executors.newFixedThreadPool(2);
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        // When: 2명의 사용자가 동시에 같은 쿠폰 사용 시도
        for (int i = 0; i < 2; i++) {
            final Long userId = (long) (i + 1);
            executorService.submit(() -> {
                try {
                    // 모든 스레드가 동시에 시작하도록 대기
                    latch.await();

                    couponService.useCoupon("TEST123456789", userId, 20000);
                    successCount.incrementAndGet();
                } catch (CouponAlreadyUsedException e) {
                    failureCount.incrementAndGet();
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                }
            });
        }

        // 모든 스레드를 동시에 시작
        latch.countDown();

        executorService.shutdown();
        executorService.awaitTermination(5, TimeUnit.SECONDS);

        // Then: 첫 번째 요청만 성공, 두 번째는 실패
        assertEquals(1, successCount.get(), "정확히 1명만 쿠폰을 사용할 수 있어야 함");
        assertEquals(1, failureCount.get(), "한 명의 요청은 실패해야 함");
    }

    @Test
    @DisplayName("쿠폰 사용 후 상태가 USED로 변경되어야 함")
    void testCouponStatusChangeToUsed() {
        // Given
        when(couponMapper.selectByCouponCodeForUpdate(anyString())).thenReturn(testCoupon);
        when(couponLogMapper.insert(any())).thenReturn(1);

        // When
        CouponLog result = couponService.useCoupon("TEST123456789", 1L, 20000);

        // Then
        assertNotNull(result, "CouponLog가 반환되어야 함");
        assertEquals(CouponStatus.USED, testCoupon.getStatus(), "쿠폰 상태가 USED로 변경되어야 함");
        assertEquals(10000, result.getDiscountApplied(), "할인금액이 올바르게 계산되어야 함");
    }

    @Test
    @DisplayName("존재하지 않는 쿠폰을 사용하려 할 때 예외 발생")
    void testUseCouponNotFound() {
        // Given
        when(couponMapper.selectByCouponCodeForUpdate(anyString())).thenReturn(null);

        // When & Then
        assertThrows(CouponNotFoundException.class, () ->
                couponService.useCoupon("INVALID_CODE", 1L, 20000)
        );
    }

    @Test
    @DisplayName("쿠폰 소유자가 아닌 사용자가 사용하려 할 때 예외 발생")
    void testUseCouponAccessDenied() {
        // Given
        when(couponMapper.selectByCouponCodeForUpdate(anyString())).thenReturn(testCoupon);

        // When & Then
        assertThrows(Exception.class, () ->
                couponService.useCoupon("TEST123456789", 999L, 20000) // 다른 userId
        );
    }

    @Test
    @DisplayName("이미 사용된 쿠폰을 다시 사용하려 할 때 예외 발생")
    void testUseCouponAlreadyUsed() {
        // Given
        testCoupon.setStatus(CouponStatus.USED);
        when(couponMapper.selectByCouponCodeForUpdate(anyString())).thenReturn(testCoupon);

        // When & Then
        assertThrows(CouponAlreadyUsedException.class, () ->
                couponService.useCoupon("TEST123456789", 1L, 20000)
        );
    }

    @Test
    @DisplayName("만료된 쿠폰을 사용하려 할 때 예외 발생")
    void testUseCouponExpired() {
        // Given
        testCoupon.getCouponInfo().setExpiredAt(LocalDateTime.now().minusDays(1));
        when(couponMapper.selectByCouponCodeForUpdate(anyString())).thenReturn(testCoupon);

        // When & Then
        assertThrows(Exception.class, () ->
                couponService.useCoupon("TEST123456789", 1L, 20000)
        );
    }
}
