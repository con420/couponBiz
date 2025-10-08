package com.kakaomobility.couponbiz.exception;

import com.kakaomobility.couponbiz.dto.response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CouponNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(CouponNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("COUPON_NOT_FOUND", ex.getMessage()));
    }

    @ExceptionHandler(CouponAccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(CouponAccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponse("ACCESS_DENIED", ex.getMessage()));
    }

    @ExceptionHandler(CouponAlreadyUsedException.class)
    public ResponseEntity<ErrorResponse> handleAlreadyUsed(CouponAlreadyUsedException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("COUPON_ALREADY_USED", ex.getMessage()));
    }

    @ExceptionHandler(CouponExpiredException.class)
    public ResponseEntity<ErrorResponse> handleExpired(CouponExpiredException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("COUPON_EXPIRED", ex.getMessage()));
    }

    @ExceptionHandler(CouponInvalidStateException.class)
    public ResponseEntity<ErrorResponse> handleInvalidState(CouponInvalidStateException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("COUPON_INVALID_STATE", ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("INTERNAL_ERROR", "서버 오류가 발생했습니다."));
    }
}