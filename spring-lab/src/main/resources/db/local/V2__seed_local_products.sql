-- Local learning fixtures only. Production migrations must not depend on this data.
INSERT INTO product (id, sku, name, price, status) VALUES
('00000000-0000-0000-0000-000000000001', 'DEMO-CREAM', '수분 크림', 18000, 'ACTIVE'),
('00000000-0000-0000-0000-000000000002', 'DEMO-TONER', '진정 토너', 12000, 'ACTIVE'),
('00000000-0000-0000-0000-000000000003', 'DEMO-DRAFT', '출시 준비 상품', 25000, 'DRAFT');
