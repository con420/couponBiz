### 기술 스택
- Spring Boot 3.2.0
- MyBatis 3.0.3
- MySQL 8.0
- Java 17
- Gradle 8.x
- Lombok

### db 스키마
1. coupon_info (쿠폰 정보)
   - coupon_info_id BIGINT PK
   - discount_type VARCHAR(10) [FIXED/RATE]
   - discount_value INT
   - created_at DATETIME
   - expired_at DATETIME
   - updated_at DATETIME

2. coupon (쿠폰)
   - coupon_id BIGINT PK
   - coupon_info_id BIGINT FK
   - user_id BIGINT
   - coupon_code VARCHAR(50) UNIQUE
   - issued_at DATETIME
   - status VARCHAR(10) [ISSUED/USED/CANCEL]
   - type VARCHAR(10) [NORMAL/CANCEL]

3. coupon_log (쿠폰 사용 로그)
   - coupon_log_id BIGINT PK
   - coupon_id BIGINT FK
   - discount_applied INT
   - used_at DATETIME
   - cancelled_at DATETIME

## 프로젝트 실행 방법

1. 테이블 생성
-- coupon_info 테이블
CREATE TABLE coupon_info (
    coupon_info_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    discount_type VARCHAR(10) NOT NULL,
    discount_value INT NOT NULL,
    created_at DATETIME NOT NULL,
    expired_at DATETIME NOT NULL,
    updated_at DATETIME
);

-- coupon 테이블
CREATE TABLE coupon (
    coupon_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    coupon_info_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    coupon_code VARCHAR(50) UNIQUE NOT NULL,
    issued_at DATETIME NOT NULL,
    status VARCHAR(10) NOT NULL,
    type VARCHAR(10) NOT NULL,
    FOREIGN KEY (coupon_info_id) REFERENCES coupon_info(coupon_info_id)
);

-- coupon_log 테이블
CREATE TABLE coupon_log (
    coupon_log_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    coupon_id BIGINT NOT NULL,
    discount_applied INT NOT NULL,
    used_at DATETIME NOT NULL,
    cancelled_at DATETIME,
    FOREIGN KEY (coupon_id) REFERENCES coupon(coupon_id)
);

2. 프로젝트 실행
./gradlew bootRun

## API 테스트 (Windows 환경)

1. 쿠폰 정보 생성 (관리자)
curl -X POST "http://localhost:8080/api/admin/coupons/info" ^
  -H "Content-Type: application/json" ^
  -d "{\"discountType\":\"FIXED\",\"discountValue\":10000,\"expiredAt\":\"2025-12-31T23:59:59\"}"

2. 쿠폰 발급
curl -X POST "http://localhost:8080/api/coupons/issue/1" ^
  -H "Content-Type: application/json" ^
  -d "{\"userId\":1}"

3. 쿠폰 단건 조회
curl -X GET "http://localhost:8080/api/coupons/ZPDYSSXXC5RS"

4. 쿠폰 사용
curl -X POST "http://localhost:8080/api/coupons/ABC123456789" ^
  -H "Content-Type: application/json" ^
  -d "{\"userId\":2,\"originalAmount\":20000}"

5. 사용자 보유 쿠폰 목록 조회
curl -X GET "http://localhost:8080/api/coupons/user/1?status=ISSUED"
