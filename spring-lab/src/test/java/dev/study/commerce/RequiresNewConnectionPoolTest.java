package dev.study.commerce;

import dev.study.commerce.transaction.OrderFlowService;
import dev.study.commerce.transaction.OrderLogService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.CannotCreateTransactionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(properties = {
        "spring.datasource.hikari.maximum-pool-size=1",
        "spring.datasource.hikari.connection-timeout=500"
})
@ActiveProfiles("test")
@Import({TestcontainersConfiguration.class, OrderFlowService.class, OrderLogService.class})
@Sql({"/sql/order-transaction.sql", "/sql/order-log.sql"})
class RequiresNewConnectionPoolTest {
    @Autowired OrderFlowService orders;
    @Autowired OrderLogService logs;
    @Autowired JdbcTemplate jdbc;

    @Test
    @DisplayName("연결이 1개면 바깥이 연결을 쥔 채 REQUIRES_NEW가 새 연결을 얻지 못한다")
    void requiresNewCannotGetSecondConnection() {
        assertThatThrownBy(() -> orders.order(logs::saveWithRequiresNew))
                .isInstanceOf(CannotCreateTransactionException.class);

        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM practice_order", Long.class)).isZero();
    }
}
