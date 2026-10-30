-- 멱등성 키를 주문에서 결제 시도로 바꾼다. 주문 하나에 결제 시도는 여러 번 있을 수 있다.
ALTER TABLE point_transaction ADD COLUMN payment_attempt_id UUID;
ALTER TABLE point_transaction DROP CONSTRAINT uk_point_transaction_order;

-- 같은 결제 시도의 사용과 사용 취소는 한 번씩만 쌓인다
ALTER TABLE point_transaction
    ADD CONSTRAINT uk_point_transaction_attempt UNIQUE (payment_attempt_id, type);

-- 같은 주문의 구매 적립은 한 번만 쌓인다
CREATE UNIQUE INDEX uk_point_transaction_order_earn ON point_transaction (order_id) WHERE type = 'EARN';
