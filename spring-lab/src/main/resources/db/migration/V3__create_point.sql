-- 적립금 1부: 원장, 적립 건, 거래 상세, 회원 잔액
CREATE TABLE point_wallet (
    member_id BIGINT PRIMARY KEY,
    balance BIGINT NOT NULL,
    CONSTRAINT ck_point_wallet_balance CHECK (balance >= 0)
);

CREATE TABLE point_transaction (
    id BIGSERIAL PRIMARY KEY,
    member_id BIGINT NOT NULL,
    type VARCHAR(20) NOT NULL,
    amount BIGINT NOT NULL,
    order_id BIGINT,
    origin_transaction_id BIGINT REFERENCES point_transaction (id),
    base_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT ck_point_transaction_type CHECK (type IN ('EARN', 'USE', 'CANCEL_USE')),
    -- 같은 주문의 사용이 두 번 쌓이지 않게 한다
    CONSTRAINT uk_point_transaction_order UNIQUE (order_id, type),
    -- 한 거래에 취소가 두 번 쌓이지 않게 한다
    CONSTRAINT uk_point_transaction_origin UNIQUE (origin_transaction_id)
);

CREATE INDEX idx_point_transaction_member ON point_transaction (member_id, created_at);

CREATE TABLE point_earning (
    id BIGSERIAL PRIMARY KEY,
    member_id BIGINT NOT NULL,
    earn_transaction_id BIGINT NOT NULL REFERENCES point_transaction (id),
    source_type VARCHAR(20) NOT NULL,
    amount BIGINT NOT NULL,
    remaining_amount BIGINT NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT ck_point_earning_source CHECK (source_type IN ('ORDER', 'REVIEW', 'EVENT')),
    CONSTRAINT ck_point_earning_amount CHECK (amount > 0 AND remaining_amount BETWEEN 0 AND amount)
);

CREATE INDEX idx_point_earning_member_expires ON point_earning (member_id, expires_at);

CREATE TABLE point_transaction_detail (
    id BIGSERIAL PRIMARY KEY,
    transaction_id BIGINT NOT NULL REFERENCES point_transaction (id),
    earning_id BIGINT NOT NULL REFERENCES point_earning (id),
    -- 적립 건에 미치는 방향: 사용은 -, 적립과 환원은 +
    amount BIGINT NOT NULL,
    CONSTRAINT ck_point_transaction_detail_amount CHECK (amount <> 0)
);

CREATE INDEX idx_point_transaction_detail_transaction ON point_transaction_detail (transaction_id);
