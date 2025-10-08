package com.kakaomobility.couponbiz.controller.api;

import com.kakaomobility.couponbiz.dto.request.CouponInfoCreateRequest;
import com.kakaomobility.couponbiz.dto.response.CouponInfoResponse;
import com.kakaomobility.couponbiz.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/coupons")
@RequiredArgsConstructor
public class CouponAdminController {
    private final CouponService couponService;

    /**
     * 쿠폰 정보 생성 (관리자)
     */
    @PostMapping("/info")
    public ResponseEntity<CouponInfoResponse> createCouponInfo(
            @RequestBody CouponInfoCreateRequest request) {
        return ResponseEntity.ok(
                CouponInfoResponse.from(
                        couponService.createCouponInfo(request.getDiscountType(), request.getDiscountValue(), request.getExpiredAt())
                )
        );
    }
}

