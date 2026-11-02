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
import org.springframework.transaction.UnexpectedRollbackException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Import({TestcontainersConfiguration.class, OrderFlowService.class, OrderLogService.class})
@Sql({"/sql/order-transaction.sql", "/sql/order-log.sql"})
class OrderRollbackRuleTest {
    @Autowired OrderFlowService orders;
    @Autowired OrderLogService logs;
    @Autowired JdbcTemplate jdbc;

    @Test
    @DisplayName("1. 체크 예외는 기본 설정에서 커밋된다")
    void checkedErrorCommitsByDefault() {
        assertThatThrownBy(logs::failWithCheckedError).isInstanceOf(Exception.class);

        assertThat(logCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("2. rollbackFor를 지정하면 체크 예외도 롤백된다")
    void rollbackForRollsBackCheckedError() {
        assertThatThrownBy(logs::failWithCheckedErrorAndRollbackFor).isInstanceOf(Exception.class);

        assertThat(logCount()).isZero();
    }

    @Test
    @DisplayName("3. 규칙이 겹치면 예외 타입에 더 가까운 규칙을 따른다")
    void closestRuleWins() {
        assertThatThrownBy(logs::failWithNoRollbackFor).isInstanceOf(IllegalStateException.class);

        assertThat(logCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("4. 트랜잭션을 시작한 메서드의 setRollbackOnly는 예외 없이 롤백된다")
    void setRollbackOnlyRollsBackQuietly() {
        logs.markRollbackOnly();

        assertThat(logCount()).isZero();
    }

    @Test
    @DisplayName("5. 참여한 메서드의 setRollbackOnly는 바깥 커밋에서 예외가 된다")
    void participantSetRollbackOnlyFailsOuterCommit() {
        assertThatThrownBy(() -> orders.order(logs::markRollbackOnly))
                .isInstanceOf(UnexpectedRollbackException.class);

        assertThat(orderCount()).isZero();
        assertThat(logCount()).isZero();
    }

    @Test
    @DisplayName("6. 안쪽 규칙이 롤백 대상이 아니라고 판단하면 표시가 남지 않아 바깥이 커밋된다")
    void noRollbackRuleLeavesNoMark() {
        orders.orderAndCatch(logs::failWithNoRollbackFor);

        assertThat(orderCount()).isEqualTo(1);
        assertThat(logCount()).isEqualTo(1);
    }

    private long orderCount() {
        return jdbc.queryForObject("SELECT COUNT(*) FROM practice_order", Long.class);
    }

    private long logCount() {
        return jdbc.queryForObject("SELECT COUNT(*) FROM practice_order_log", Long.class);
    }
}
