package com.kakaomobility.couponbiz.mapper;

import com.kakaomobility.couponbiz.model.Coupon;
import com.kakaomobility.couponbiz.model.CouponStatus;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface CouponMapper {
    void insert(Coupon coupon);
    Coupon selectByCouponCode(String couponCode);
    List<Coupon> selectByUserId(Long userId);
    List<Coupon> selectByUserIdAndStatus(@Param("userId") Long userId, @Param("status") CouponStatus status);
    void updateStatus(Coupon coupon);
    void updateType(Coupon coupon);
    List<Coupon> selectExpiredCoupons(LocalDateTime now);
}
