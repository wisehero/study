package dev.study.commerce;

import dev.study.commerce.transaction.OrderFlowService;
import dev.study.commerce.transaction.OrderLogService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.NestedTransactionNotSupportedException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.UnexpectedRollbackException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Import({TestcontainersConfiguration.class, OrderFlowService.class, OrderLogService.class})
@Sql({"/sql/order-transaction.sql", "/sql/order-log.sql"})
class OrderPropagationTest {
    @Autowired OrderFlowService orders;
    @Autowired OrderLogService logs;
    @Autowired JdbcTemplate jdbc;
    @Autowired PlatformTransactionManager transactionManager;

    @Test
    @DisplayName("1. REQUIRED: 바깥과 같은 연결, 같은 트랜잭션을 쓴다")
    void requiredUsesSameTransaction() {
        OrderFlowService.TxPair pair = orders.compare(logs::saveWithRequired);

        assertThat(pair.inner()).isEqualTo(pair.outer());
    }

    @Test
    @DisplayName("2. REQUIRES_NEW: 다른 연결, 다른 트랜잭션을 쓴다")
    void requiresNewUsesOtherTransaction() {
        OrderFlowService.TxPair pair = orders.compare(logs::saveWithRequiresNew);

        assertThat(pair.inner().connectionId()).isNotEqualTo(pair.outer().connectionId());
        assertThat(pair.inner().transactionId()).isNotEqualTo(pair.outer().transactionId());
    }

    @Test
    @DisplayName("3. REQUIRED 안쪽 실패를 바깥에서 잡아도 커밋 시점에 전체 롤백된다")
    void caughtRequiredFailureRollsBackAll() {
        assertThatThrownBy(() -> orders.orderAndCatch(logs::failWithRequired))
                .isInstanceOf(UnexpectedRollbackException.class)
                .hasMessage("Transaction silently rolled back because it has been marked as rollback-only");

        assertThat(orderCount()).isZero();
        assertThat(logCount()).isZero();
    }

    @Test
    @DisplayName("4. REQUIRES_NEW 안쪽 실패를 바깥에서 잡으면 안쪽만 롤백된다")
    void caughtRequiresNewFailureKeepsOrder() {
        orders.orderAndCatch(logs::failWithRequiresNew);

        assertThat(orderCount()).isEqualTo(1);
        assertThat(logCount()).isZero();
    }

    @Test
    @DisplayName("5. REQUIRES_NEW 안쪽 커밋은 바깥이 실패해도 남는다")
    void requiresNewCommitSurvivesOuterFailure() {
        assertThatThrownBy(() -> orders.orderAndFail(logs::saveWithRequiresNew))
                .isInstanceOf(IllegalStateException.class);

        assertThat(orderCount()).isZero();
        assertThat(logCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("6. 이 앱의 JpaTransactionManager는 기본 설정에서 NESTED를 거부한다")
    void jpaTransactionManagerRejectsNested() {
        assertThat(transactionManager).isInstanceOf(JpaTransactionManager.class);

        assertThatThrownBy(() -> orders.order(logs::saveWithNested))
                .isInstanceOf(NestedTransactionNotSupportedException.class);

        assertThat(orderCount()).isZero();
        assertThat(logCount()).isZero();
    }

    @Test
    @DisplayName("7. 허용 설정을 켜도 Hibernate 환경에서는 저장점을 만들지 못한다")
    void hibernateHasNoSavepointEvenWhenAllowed() {
        JpaTransactionManager jpaManager = (JpaTransactionManager) transactionManager;
        // 테스트끼리 공유하는 빈이므로 이 호출 동안만 설정을 바꾼다.
        jpaManager.setNestedTransactionAllowed(true);
        try {
            assertThatThrownBy(() -> orders.order(logs::saveWithNested))
                    .isInstanceOf(NestedTransactionNotSupportedException.class)
                    .hasMessageContaining("JpaDialect does not support savepoints");
        } finally {
            jpaManager.setNestedTransactionAllowed(false);
        }
    }

    private long orderCount() {
        return jdbc.queryForObject("SELECT COUNT(*) FROM practice_order", Long.class);
    }

    private long logCount() {
        return jdbc.queryForObject("SELECT COUNT(*) FROM practice_order_log", Long.class);
    }
}
