package dev.study.commerce;

import javax.sql.DataSource;

import dev.study.commerce.transaction.OrderFlowService;
import dev.study.commerce.transaction.OrderLogService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.UnexpectedRollbackException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Import({TestcontainersConfiguration.class, OrderFlowService.class, OrderLogService.class,
        JdbcManagerPropagationTest.JdbcTransactionConfig.class})
@Sql({"/sql/order-transaction.sql", "/sql/order-log.sql"})
class JdbcManagerPropagationTest {
    // NESTED의 저장점을 확인하려고 이 테스트에서만 JDBC용 트랜잭션 매니저로 바꾼다.
    @TestConfiguration(proxyBeanMethods = false)
    static class JdbcTransactionConfig {
        @Bean
        DataSourceTransactionManager transactionManager(DataSource dataSource) {
            return new DataSourceTransactionManager(dataSource);
        }
    }

    @Autowired OrderFlowService orders;
    @Autowired OrderLogService logs;
    @Autowired JdbcTemplate jdbc;

    @Test
    @DisplayName("1. NESTED: 바깥과 같은 연결, 같은 트랜잭션을 쓴다")
    void nestedUsesSameTransaction() {
        OrderFlowService.TxPair pair = orders.compare(logs::saveWithNested);

        assertThat(pair.inner()).isEqualTo(pair.outer());
    }

    @Test
    @DisplayName("2. NESTED 안쪽 실패를 바깥에서 잡으면 저장점까지만 되돌린다")
    void nestedFailureRollsBackToSavepoint() {
        orders.orderAndCatch(logs::failWithNested);

        assertThat(orderCount()).isEqualTo(1);
        assertThat(logCount()).isZero();
    }

    @Test
    @DisplayName("3. NESTED 안쪽이 성공해도 바깥이 실패하면 함께 롤백된다")
    void nestedWorkRollsBackWithOuter() {
        assertThatThrownBy(() -> orders.orderAndFail(logs::saveWithNested))
                .isInstanceOf(IllegalStateException.class);

        assertThat(orderCount()).isZero();
        assertThat(logCount()).isZero();
    }

    @Test
    @DisplayName("4. JDBC 매니저는 rollback-only를 커밋 전에 확인해 바로 롤백한다")
    void jdbcManagerRollsBackBeforeCommit() {
        assertThatThrownBy(() -> orders.orderAndCatch(logs::failWithRequired))
                .isInstanceOf(UnexpectedRollbackException.class)
                .hasMessage("Transaction rolled back because it has been marked as rollback-only");

        assertThat(orderCount()).isZero();
        assertThat(logCount()).isZero();
    }

    private long orderCount() {
        return jdbc.queryForObject("SELECT COUNT(*) FROM practice_order", Long.class);
    }

    private long logCount() {
        return jdbc.queryForObject("SELECT COUNT(*) FROM practice_order_log", Long.class);
    }
}
