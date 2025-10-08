package com.kakaomobility.couponbiz.mapper;

import com.kakaomobility.couponbiz.model.CouponInfo;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CouponInfoMapper {
    void insert(CouponInfo couponInfo);
    CouponInfo selectById(Long couponInfoId);
    void update(CouponInfo couponInfo);
}
