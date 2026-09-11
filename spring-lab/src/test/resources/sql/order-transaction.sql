-- 테스트 전용 PostgreSQL에서 각 케이스를 주문 0건, 재고 10개로 시작한다.
CREATE TABLE IF NOT EXISTS practice_stock (
    product_code VARCHAR(40) PRIMARY KEY,
    quantity INTEGER NOT NULL CHECK (quantity >= 0)
);

CREATE TABLE IF NOT EXISTS practice_order (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    product_code VARCHAR(40) NOT NULL REFERENCES practice_stock (product_code),
    quantity INTEGER NOT NULL CHECK (quantity > 0)
);

DELETE FROM practice_order;
DELETE FROM practice_stock;
INSERT INTO practice_stock (product_code, quantity) VALUES ('CREAM', 10);
