-- 전파 실습에서 안쪽 트랜잭션의 변경을 확인하는 테스트 전용 테이블이다.
CREATE TABLE IF NOT EXISTS practice_order_log (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    message VARCHAR(100) NOT NULL
);

DELETE FROM practice_order_log;
