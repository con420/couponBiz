package com.kakaomobility.couponbiz;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CouponBizApplication {

    public static void main(String[] args) {
        SpringApplication.run(CouponBizApplication.class, args);
    }

}
