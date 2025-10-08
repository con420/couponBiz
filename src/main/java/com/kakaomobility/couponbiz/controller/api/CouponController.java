package com.kakaomobility.couponbiz.controller.api;

import com.kakaomobility.couponbiz.dto.request.CouponIssueRequest;
import com.kakaomobility.couponbiz.dto.request.CouponUseRequest;
import com.kakaomobility.couponbiz.dto.response.CouponLogResponse;
import com.kakaomobility.couponbiz.dto.response.CouponResponse;
import com.kakaomobility.couponbiz.model.CouponStatus;
import com.kakaomobility.couponbiz.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
public class CouponController {
    private final CouponService couponService;

    /**
     * 쿠폰 발급
     */
    @PostMapping("/issue/{couponInfoId}")
    public ResponseEntity<CouponResponse> issueCoupon(
            @PathVariable Long couponInfoId,
            @RequestBody CouponIssueRequest request) {
        return ResponseEntity.ok(
                CouponResponse.from(
                        couponService.issueCoupon(couponInfoId, request.getUserId())
                )
        );
    }

    /**
     * 쿠폰 조회
     */
    @GetMapping("/{code}")
    public ResponseEntity<CouponResponse> getCoupon(@PathVariable String code) {
        return ResponseEntity.ok(CouponResponse.from(couponService.getCoupon(code)));
    }

    /**
     * 쿠폰 사용
     */
    @PostMapping("/{code}")
    public ResponseEntity<CouponLogResponse> useCoupon(
            @PathVariable String code,
            @RequestBody CouponUseRequest request) {
        return ResponseEntity.ok(
                CouponLogResponse.from(
                        couponService.useCoupon(code, request.getUserId(), request.getOriginalAmount())
                )
        );
    }

    /**
     * 사용자 쿠폰 목록 조회
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<CouponResponse>> getUserCoupons(
            @PathVariable Long userId,
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(
                couponService.getUserCoupons(userId, status != null ? CouponStatus.valueOf(status) : null).stream()
                        .map(CouponResponse::from)
                        .collect(Collectors.toList())
        );
    }
}