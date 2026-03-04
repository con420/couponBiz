package com.kakaomobility.couponbiz.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kakaomobility.couponbiz.controller.api.CouponController;
import com.kakaomobility.couponbiz.dto.request.CouponIssueRequest;
import com.kakaomobility.couponbiz.dto.request.CouponUseRequest;
import com.kakaomobility.couponbiz.model.*;
import com.kakaomobility.couponbiz.service.CouponService;
import com.kakaomobility.couponbiz.exception.CouponNotFoundException;
import com.kakaomobility.couponbiz.exception.CouponAlreadyUsedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * CouponController 통합 테스트
 *
 * 테스트 대상:
 * 1. 쿠폰 발급 API
 * 2. 쿠폰 조회 API
 * 3. 쿠폰 사용 API
 * 4. 사용자 쿠폰 목록 조회 API
 */
@WebMvcTest(CouponController.class)
@DisplayName("쿠폰 컨트롤러 API 테스트")
class CouponControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CouponService couponService;

    private CouponInfo testCouponInfo;
    private Coupon testCoupon;
    private CouponLog testCouponLog;

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

        testCouponLog = CouponLog.builder()
                .couponLogId(1L)
                .coupon(testCoupon)
                .discountApplied(10000)
                .usedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("POST /api/coupons/issue/{id} - 쿠폰 발급 성공")
    void testIssueCoupon() throws Exception {
        // Given
        Long couponInfoId = 1L;
        CouponIssueRequest request = new CouponIssueRequest();
        request.setUserId(1L);

        when(couponService.issueCoupon(couponInfoId, 1L)).thenReturn(testCoupon);

        // When & Then
        mockMvc.perform(post("/api/coupons/issue/{couponInfoId}", couponInfoId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.couponId", is(1)))
                .andExpect(jsonPath("$.couponCode", is("ABC123456789")))
                .andExpect(jsonPath("$.status", is("ISSUED")))
                .andExpect(jsonPath("$.userId", is(1)));

        verify(couponService, times(1)).issueCoupon(couponInfoId, 1L);
    }

    @Test
    @DisplayName("GET /api/coupons/{code} - 쿠폰 조회 성공")
    void testGetCoupon() throws Exception {
        // Given
        when(couponService.getCoupon("ABC123456789")).thenReturn(testCoupon);

        // When & Then
        mockMvc.perform(get("/api/coupons/{code}", "ABC123456789"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.couponId", is(1)))
                .andExpect(jsonPath("$.couponCode", is("ABC123456789")))
                .andExpect(jsonPath("$.status", is("ISSUED")));

        verify(couponService, times(1)).getCoupon("ABC123456789");
    }

    @Test
    @DisplayName("GET /api/coupons/{code} - 존재하지 않는 쿠폰 조회 시 404 반환")
    void testGetCouponNotFound() throws Exception {
        // Given
        when(couponService.getCoupon(anyString())).thenThrow(new CouponNotFoundException("존재하지 않는 쿠폰입니다"));

        // When & Then
        mockMvc.perform(get("/api/coupons/{code}", "INVALID_CODE"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code", is("COUPON_NOT_FOUND")));
    }

    @Test
    @DisplayName("POST /api/coupons/{code} - 쿠폰 사용 성공")
    void testUseCoupon() throws Exception {
        // Given
        CouponUseRequest request = new CouponUseRequest();
        request.setUserId(1L);
        request.setOriginalAmount(20000);

        when(couponService.useCoupon("ABC123456789", 1L, 20000)).thenReturn(testCouponLog);

        // When & Then
        mockMvc.perform(post("/api/coupons/{code}", "ABC123456789")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.couponLogId", is(1)))
                .andExpect(jsonPath("$.discountApplied", is(10000)));

        verify(couponService, times(1)).useCoupon("ABC123456789", 1L, 20000);
    }

    @Test
    @DisplayName("POST /api/coupons/{code} - 이미 사용된 쿠폰 사용 시도 시 400 반환")
    void testUseCouponAlreadyUsed() throws Exception {
        // Given
        CouponUseRequest request = new CouponUseRequest();
        request.setUserId(1L);
        request.setOriginalAmount(20000);

        when(couponService.useCoupon(anyString(), anyLong(), anyInt()))
                .thenThrow(new CouponAlreadyUsedException("이미 사용된 쿠폰입니다."));

        // When & Then
        mockMvc.perform(post("/api/coupons/{code}", "ABC123456789")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is("COUPON_ALREADY_USED")));
    }

    @Test
    @DisplayName("GET /api/coupons/user/{userId} - 사용자의 전체 쿠폰 조회")
    void testGetUserCouponsAll() throws Exception {
        // Given
        List<Coupon> coupons = Arrays.asList(testCoupon, testCoupon);
        when(couponService.getUserCoupons(1L, null)).thenReturn(coupons);

        // When & Then
        mockMvc.perform(get("/api/coupons/user/{userId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].couponCode", is("ABC123456789")))
                .andExpect(jsonPath("$[1].couponCode", is("ABC123456789")));

        verify(couponService, times(1)).getUserCoupons(1L, null);
    }

    @Test
    @DisplayName("GET /api/coupons/user/{userId}?status=ISSUED - 사용자의 특정 상태 쿠폰 조회")
    void testGetUserCouponsByStatus() throws Exception {
        // Given
        List<Coupon> coupons = Arrays.asList(testCoupon);
        when(couponService.getUserCoupons(1L, CouponStatus.ISSUED)).thenReturn(coupons);

        // When & Then
        mockMvc.perform(get("/api/coupons/user/{userId}", 1L)
                .param("status", "ISSUED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].status", is("ISSUED")));

        verify(couponService, times(1)).getUserCoupons(1L, CouponStatus.ISSUED);
    }

    @Test
    @DisplayName("GET /api/coupons/user/{userId} - 사용자의 쿠폰이 없으면 빈 배열 반환")
    void testGetUserCouponsEmpty() throws Exception {
        // Given
        when(couponService.getUserCoupons(anyLong(), isNull())).thenReturn(Arrays.asList());

        // When & Then
        mockMvc.perform(get("/api/coupons/user/{userId}", 999L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}
