package dev.study.commerce.transaction;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

// 테스트에서 Spring 빈으로 등록하는 실습용 서비스다.
public class OrderService {
    private final JdbcTemplate jdbc;

    public OrderService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Transactional
    public void order(String productCode, int quantity) {
        saveOrder(productCode, quantity);
        reduceStock(productCode, quantity);
    }

    @Transactional
    public boolean orderWithCaughtError(String productCode, int quantity) {
        saveOrder(productCode, quantity);
        try {
            reduceStock(productCode, quantity);
            return true;
        } catch (IllegalStateException error) {
            // 의도적인 문제 예제: false 반환은 트랜잭션 롤백을 요청하지 않는다.
            return false;
        }
    }

    private void saveOrder(String productCode, int quantity) {
        jdbc.update("INSERT INTO practice_order (product_code, quantity) VALUES (?, ?)",
                productCode, quantity);
    }

    private void reduceStock(String productCode, int quantity) {
        int stock = jdbc.queryForObject(
                "SELECT quantity FROM practice_stock WHERE product_code = ?",
                Integer.class, productCode);
        if (stock < quantity) {
            // DB 오류가 아니라, 서비스 안에서 직접 발생시키는 런타임 예외다.
            throw new IllegalStateException("재고가 부족합니다.");
        }
        jdbc.update("UPDATE practice_stock SET quantity = quantity - ? WHERE product_code = ?",
                quantity, productCode);
    }
}
