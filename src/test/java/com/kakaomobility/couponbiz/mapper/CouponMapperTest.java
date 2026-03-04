package com.kakaomobility.couponbiz.mapper;

import com.kakaomobility.couponbiz.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CouponMapper 통합 테스트
 *
 * 테스트 데이터베이스: H2 (In-memory)
 * 테스트 대상:
 * 1. 쿠폰 삽입 (insert)
 * 2. 쿠폰 조회 (selectByCouponCode, selectByCouponCodeForUpdate)
 * 3. 쿠폰 상태 업데이트 (updateStatus)
 * 4. 사용자별 쿠폰 조회 (selectByUserId, selectByUserIdAndStatus)
 * 5. 만료된 쿠폰 조회 (selectExpiredCoupons)
 */
@SpringBootTest
@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(locations = "classpath:application-test.properties")
@DisplayName("쿠폰 매퍼 통합 테스트")
class CouponMapperTest {

    @Autowired
    private CouponMapper couponMapper;

    @Autowired
    private CouponInfoMapper couponInfoMapper;

    private CouponInfo testCouponInfo;

    @BeforeEach
    void setUp() {
        // 테스트용 쿠폰 정보 생성
        testCouponInfo = CouponInfo.builder()
                .discountType(DiscountType.FIXED)
                .discountValue(10000)
                .createdAt(LocalDateTime.now())
                .expiredAt(LocalDateTime.now().plusDays(30))
                .updatedAt(LocalDateTime.now())
                .build();

        couponInfoMapper.insert(testCouponInfo);
    }

    @Test
    @DisplayName("쿠폰 삽입 성공")
    void testInsertCoupon() {
        // Given
        Coupon coupon = Coupon.builder()
                .couponInfo(testCouponInfo)
                .userId(1L)
                .couponCode("TEST123456789")
                .issuedAt(LocalDateTime.now())
                .status(CouponStatus.ISSUED)
                .type(CouponType.NORMAL)
                .build();

        // When
        couponMapper.insert(coupon);

        // Then
        assertNotNull(coupon.getCouponId(), "쿠폰 ID가 생성되어야 함");
        Coupon foundCoupon = couponMapper.selectByCouponCode("TEST123456789");
        assertNotNull(foundCoupon);
        assertEquals("TEST123456789", foundCoupon.getCouponCode());
    }

    @Test
    @DisplayName("쿠폰 조회 성공 (selectByCouponCode)")
    void testSelectByCouponCode() {
        // Given
        Coupon coupon = Coupon.builder()
                .couponInfo(testCouponInfo)
                .userId(1L)
                .couponCode("SEARCH123456")
                .issuedAt(LocalDateTime.now())
                .status(CouponStatus.ISSUED)
                .type(CouponType.NORMAL)
                .build();
        couponMapper.insert(coupon);

        // When
        Coupon result = couponMapper.selectByCouponCode("SEARCH123456");

        // Then
        assertNotNull(result);
        assertEquals("SEARCH123456", result.getCouponCode());
        assertEquals(1L, result.getUserId());
        assertEquals(CouponStatus.ISSUED, result.getStatus());
        assertNotNull(result.getCouponInfo());
    }

    @Test
    @DisplayName("존재하지 않는 쿠폰 조회 시 null 반환")
    void testSelectByCouponCodeNotFound() {
        // When
        Coupon result = couponMapper.selectByCouponCode("INVALID_CODE");

        // Then
        assertNull(result);
    }

    @Test
    @DisplayName("비관적 잠금 쿠폰 조회 성공 (selectByCouponCodeForUpdate)")
    void testSelectByCouponCodeForUpdate() {
        // Given
        Coupon coupon = Coupon.builder()
                .couponInfo(testCouponInfo)
                .userId(1L)
                .couponCode("LOCK123456789")
                .issuedAt(LocalDateTime.now())
                .status(CouponStatus.ISSUED)
                .type(CouponType.NORMAL)
                .build();
        couponMapper.insert(coupon);

        // When
        Coupon result = couponMapper.selectByCouponCodeForUpdate("LOCK123456789");

        // Then
        assertNotNull(result);
        assertEquals("LOCK123456789", result.getCouponCode());
        // FOR UPDATE 쿼리는 행 잠금을 획득하므로 정상 조회와 결과는 동일
    }

    @Test
    @DisplayName("쿠폰 상태 업데이트")
    void testUpdateStatus() {
        // Given
        Coupon coupon = Coupon.builder()
                .couponInfo(testCouponInfo)
                .userId(1L)
                .couponCode("UPDATE123456")
                .issuedAt(LocalDateTime.now())
                .status(CouponStatus.ISSUED)
                .type(CouponType.NORMAL)
                .build();
        couponMapper.insert(coupon);

        // When
        coupon.setStatus(CouponStatus.USED);
        couponMapper.updateStatus(coupon);

        // Then
        Coupon updatedCoupon = couponMapper.selectByCouponCode("UPDATE123456");
        assertEquals(CouponStatus.USED, updatedCoupon.getStatus());
    }

    @Test
    @DisplayName("사용자별 전체 쿠폰 조회")
    void testSelectByUserId() {
        // Given
        Coupon coupon1 = Coupon.builder()
                .couponInfo(testCouponInfo)
                .userId(1L)
                .couponCode("USER001")
                .issuedAt(LocalDateTime.now())
                .status(CouponStatus.ISSUED)
                .type(CouponType.NORMAL)
                .build();

        Coupon coupon2 = Coupon.builder()
                .couponInfo(testCouponInfo)
                .userId(1L)
                .couponCode("USER002")
                .issuedAt(LocalDateTime.now())
                .status(CouponStatus.USED)
                .type(CouponType.NORMAL)
                .build();

        couponMapper.insert(coupon1);
        couponMapper.insert(coupon2);

        // When
        List<Coupon> coupons = couponMapper.selectByUserId(1L);

        // Then
        assertNotNull(coupons);
        assertEquals(2, coupons.size());
        assertTrue(coupons.stream().allMatch(c -> c.getUserId().equals(1L)));
    }

    @Test
    @DisplayName("사용자별 특정 상태 쿠폰 조회")
    void testSelectByUserIdAndStatus() {
        // Given
        Coupon coupon1 = Coupon.builder()
                .couponInfo(testCouponInfo)
                .userId(2L)
                .couponCode("STATUS001")
                .issuedAt(LocalDateTime.now())
                .status(CouponStatus.ISSUED)
                .type(CouponType.NORMAL)
                .build();

        Coupon coupon2 = Coupon.builder()
                .couponInfo(testCouponInfo)
                .userId(2L)
                .couponCode("STATUS002")
                .issuedAt(LocalDateTime.now())
                .status(CouponStatus.USED)
                .type(CouponType.NORMAL)
                .build();

        couponMapper.insert(coupon1);
        couponMapper.insert(coupon2);

        // When
        List<Coupon> issuedCoupons = couponMapper.selectByUserIdAndStatus(2L, CouponStatus.ISSUED);
        List<Coupon> usedCoupons = couponMapper.selectByUserIdAndStatus(2L, CouponStatus.USED);

        // Then
        assertEquals(1, issuedCoupons.size());
        assertEquals(CouponStatus.ISSUED, issuedCoupons.get(0).getStatus());

        assertEquals(1, usedCoupons.size());
        assertEquals(CouponStatus.USED, usedCoupons.get(0).getStatus());
    }

    @Test
    @DisplayName("만료된 쿠폰 조회")
    void testSelectExpiredCoupons() {
        // Given
        CouponInfo expiredCouponInfo = CouponInfo.builder()
                .discountType(DiscountType.FIXED)
                .discountValue(5000)
                .createdAt(LocalDateTime.now().minusDays(40))
                .expiredAt(LocalDateTime.now().minusDays(10)) // 10일 전에 만료
                .updatedAt(LocalDateTime.now().minusDays(10))
                .build();
        couponInfoMapper.insert(expiredCouponInfo);

        Coupon expiredCoupon = Coupon.builder()
                .couponInfo(expiredCouponInfo)
                .userId(3L)
                .couponCode("EXPIRED123456")
                .issuedAt(LocalDateTime.now().minusDays(40))
                .status(CouponStatus.ISSUED)
                .type(CouponType.NORMAL)
                .build();
        couponMapper.insert(expiredCoupon);

        // When
        List<Coupon> expiredCoupons = couponMapper.selectExpiredCoupons(LocalDateTime.now());

        // Then
        assertNotNull(expiredCoupons);
        assertEquals(1, expiredCoupons.size());
        assertEquals(CouponStatus.ISSUED, expiredCoupons.get(0).getStatus());
    }

    @Test
    @DisplayName("만료되지 않은 쿠폰은 조회되지 않음")
    void testSelectExpiredCouponsExcludesValid() {
        // Given
        Coupon validCoupon = Coupon.builder()
                .couponInfo(testCouponInfo) // 30일 뒤에 만료되는 쿠폰
                .userId(4L)
                .couponCode("VALID123456")
                .issuedAt(LocalDateTime.now())
                .status(CouponStatus.ISSUED)
                .type(CouponType.NORMAL)
                .build();
        couponMapper.insert(validCoupon);

        // When
        List<Coupon> expiredCoupons = couponMapper.selectExpiredCoupons(LocalDateTime.now());

        // Then
        assertNotNull(expiredCoupons);
        assertTrue(expiredCoupons.stream()
                .noneMatch(c -> c.getCouponCode().equals("VALID123456")));
    }

    @Test
    @DisplayName("쿠폰 타입 업데이트")
    void testUpdateType() {
        // Given
        Coupon coupon = Coupon.builder()
                .couponInfo(testCouponInfo)
                .userId(5L)
                .couponCode("TYPE123456")
                .issuedAt(LocalDateTime.now())
                .status(CouponStatus.ISSUED)
                .type(CouponType.NORMAL)
                .build();
        couponMapper.insert(coupon);

        // When
        coupon.setType(CouponType.CANCEL);
        couponMapper.updateType(coupon);

        // Then
        Coupon updatedCoupon = couponMapper.selectByCouponCode("TYPE123456");
        assertEquals(CouponType.CANCEL, updatedCoupon.getType());
    }
}
