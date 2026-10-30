package dev.study.commerce;

import dev.study.commerce.point.application.PointService;
import dev.study.commerce.point.domain.EarningSource;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
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
@Import(TestcontainersConfiguration.class)
@Sql("/sql/point-reset.sql")
// 동시 요청을 보내야 하므로 테스트에 @Transactional을 붙이지 않는다.
class PointServiceTest {
    static final long MEMBER = 1L;

    @Autowired PointService points;
    @Autowired JdbcTemplate jdbc;

    @AfterEach
    void ledgerMatchesWalletAndEarnings() {
        // 2부의 검증식: 잔액 = 적립 건 합계 = 원장 합계
        long balance = sum("SELECT COALESCE(SUM(balance), 0) FROM point_wallet");
        assertThat(sum("SELECT COALESCE(SUM(remaining_amount), 0) FROM point_earning")).isEqualTo(balance);
        assertThat(sum("SELECT COALESCE(SUM(amount), 0) FROM point_transaction")).isEqualTo(balance);
    }

    @Test
    @DisplayName("1. 사용: 1,500원을 만료가 임박한 적립 건부터 꺼낸다")
    void useTakesSoonestExpiringFirst() {
        long later = earn(1_000, 60);
        long sooner = earn(1_000, 30);

        points.use(MEMBER, UUID.randomUUID(), 100L, 1_500);

        assertThat(remaining(sooner)).isZero();
        assertThat(remaining(later)).isEqualTo(500);
        assertThat(balance()).isEqualTo(500);
        assertThat(jdbc.queryForList("SELECT amount FROM point_transaction_detail WHERE amount < 0 ORDER BY id",
                Long.class)).containsExactly(-1_000L, -500L);
    }

    @Test
    @DisplayName("2. 잔액 부족: 예외가 나면 아무것도 바뀌지 않는다")
    void insufficientBalanceChangesNothing() {
        earn(1_000, 30);

        assertThatThrownBy(() -> points.use(MEMBER, UUID.randomUUID(), 100L, 1_500))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("적립금이 부족합니다.");

        assertThat(balance()).isEqualTo(1_000);
        assertThat(sum("SELECT COUNT(*) FROM point_transaction WHERE type = 'USE'")).isZero();
    }

    @Test
    @DisplayName("3. 사용 취소: 쓴 적립 건에 되돌리고, 두 번 취소해도 한 번만 환원한다")
    void cancelRestoresEarningsOnce() {
        long sooner = earn(1_000, 30);
        long later = earn(1_000, 60);
        UUID attempt = UUID.randomUUID();
        points.use(MEMBER, attempt, 100L, 1_500);

        long first = points.cancelUse(attempt);
        long second = points.cancelUse(attempt);

        assertThat(second).isEqualTo(first);
        assertThat(remaining(sooner)).isEqualTo(1_000);
        assertThat(remaining(later)).isEqualTo(1_000);
        assertThat(balance()).isEqualTo(2_000);
    }

    @Test
    @DisplayName("4. 중복 요청: 같은 결제 시도 키로 두 번 사용해도 한 번만 차감한다")
    void sameAttemptIsUsedOnce() {
        earn(1_000, 30);
        UUID attempt = UUID.randomUUID();

        long first = points.use(MEMBER, attempt, 100L, 800);
        long second = points.use(MEMBER, attempt, 100L, 800);

        assertThat(second).isEqualTo(first);
        assertThat(balance()).isEqualTo(200);
        assertThatThrownBy(() -> points.use(MEMBER, attempt, 100L, 500))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("5. 동시 사용: 잔액 1,000원에 800원 결제 10건이 동시에 와도 1건만 성공한다")
    void concurrentUsesDoNotOverspend() throws Exception {
        earn(1_000, 30);
        int requests = 10;
        var start = new CountDownLatch(1);
        var succeeded = new AtomicInteger();
        var failed = new AtomicInteger();
        var futures = new ArrayList<Future<?>>();

        try (ExecutorService executor = Executors.newFixedThreadPool(requests)) {
            for (long order = 1; order <= requests; order++) {
                long orderId = order;
                futures.add(executor.submit(() -> {
                    start.await();
                    try {
                        points.use(MEMBER, UUID.randomUUID(), orderId, 800);
                        succeeded.incrementAndGet();
                    } catch (IllegalStateException insufficient) {
                        failed.incrementAndGet();
                    }
                    return null;
                }));
            }
            start.countDown();
            for (Future<?> future : futures) {
                future.get(10, TimeUnit.SECONDS);
            }
        }

        assertThat(succeeded).hasValue(1);
        assertThat(failed).hasValue(requests - 1);
        assertThat(balance()).isEqualTo(200);
    }

    @Test
    @DisplayName("6. 재결제: PG 실패로 사용을 취소한 주문을 다시 결제하면 다시 차감한다")
    void retryAfterCanceledUseDeductsAgain() {
        earn(1_000, 30);
        UUID failedAttempt = UUID.randomUUID();
        points.use(MEMBER, failedAttempt, 100L, 800);
        points.cancelUse(failedAttempt);

        points.use(MEMBER, UUID.randomUUID(), 100L, 800);

        assertThat(balance()).isEqualTo(200);
    }

    private long earn(long amount, int days) {
        points.earn(MEMBER, amount, EarningSource.EVENT, null, Instant.now().plus(Duration.ofDays(days)));
        return sum("SELECT MAX(id) FROM point_earning");
    }

    private long remaining(long earningId) {
        return jdbc.queryForObject("SELECT remaining_amount FROM point_earning WHERE id = ?", Long.class, earningId);
    }

    private long balance() {
        return jdbc.queryForObject("SELECT balance FROM point_wallet WHERE member_id = ?", Long.class, MEMBER);
    }

    private long sum(String sql) {
        return jdbc.queryForObject(sql, Long.class);
    }
}
