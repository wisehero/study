package dev.study.commerce;

import dev.study.commerce.transaction.OrderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Import({TestcontainersConfiguration.class, OrderService.class})
@Sql("/sql/order-transaction.sql")
// 테스트에는 @Transactional을 붙이지 않는다. 서비스 트랜잭션이 끝난 뒤 DB를 조회한다.
class OrderTransactionTest {
    @Autowired OrderService orders;
    @Autowired JdbcTemplate jdbc;

    @Test
    @DisplayName("1. 정상 처리: 주문 1건 저장, 재고 10 → 7")
    void successSavesOrderAndReducesStock() {
        orders.order("CREAM", 3);

        assertThat(orderCount()).isEqualTo(1);
        assertThat(orderedQuantity()).isEqualTo(3);
        assertThat(stock()).isEqualTo(7);
    }

    @Test
    @DisplayName("2. 예외 전달: 주문 롤백, 재고 10 유지")
    void errorRollsBackOrder() {
        assertThatThrownBy(() -> orders.order("CREAM", 11))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("재고가 부족합니다.");

        assertThat(orderCount()).isZero();
        assertThat(stock()).isEqualTo(10);
    }

    @Test
    @DisplayName("3. 예외를 잡고 반환: false여도 주문 1건 저장, 재고 10 유지")
    void caughtErrorStillSavesOrder() {
        boolean success = orders.orderWithCaughtError("CREAM", 11);

        assertThat(success).isFalse();
        assertThat(orderCount()).isEqualTo(1);
        assertThat(orderedQuantity()).isEqualTo(11);
        assertThat(stock()).isEqualTo(10);
    }

    private long orderCount() {
        return jdbc.queryForObject("SELECT COUNT(*) FROM practice_order", Long.class);
    }

    private int orderedQuantity() {
        return jdbc.queryForObject("SELECT quantity FROM practice_order", Integer.class);
    }

    private int stock() {
        return jdbc.queryForObject(
                "SELECT quantity FROM practice_stock WHERE product_code = 'CREAM'", Integer.class);
    }
}
