package com.kakaomobility.couponbiz.mapper;

import com.kakaomobility.couponbiz.model.CouponLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CouponLogMapper {
    void insert(CouponLog couponLog);
    CouponLog selectById(Long couponLogId);
    CouponLog selectActiveByCouponId(Long couponId);
    void updateCancelledAt(CouponLog couponLog);
}
