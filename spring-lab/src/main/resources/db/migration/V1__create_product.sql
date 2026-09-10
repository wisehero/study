CREATE TABLE product (
    id UUID PRIMARY KEY,
    sku VARCHAR(40) NOT NULL,
    name VARCHAR(120) NOT NULL,
    price BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_product_sku UNIQUE (sku),
    CONSTRAINT ck_product_price CHECK (price >= 0),
    CONSTRAINT ck_product_status CHECK (status IN ('DRAFT', 'ACTIVE', 'SUSPENDED'))
);

CREATE INDEX idx_product_status_id ON product (status, id);
