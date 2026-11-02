package dev.study.commerce.transaction;

import java.util.function.Supplier;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

// 바깥 트랜잭션 역할을 하는 실습용 서비스다. 안쪽 호출은 테스트에서 전달받는다.
public class OrderFlowService {
    public record TxPair(TxInfo outer, TxInfo inner) {
    }

    private final JdbcTemplate jdbc;

    public OrderFlowService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Transactional
    public TxPair compare(Supplier<TxInfo> innerWork) {
        saveOrder();
        TxInfo outer = TxInfoReader.read(jdbc);
        return new TxPair(outer, innerWork.get());
    }

    @Transactional
    public void orderAndCatch(Runnable innerWork) {
        saveOrder();
        try {
            innerWork.run();
        } catch (IllegalStateException error) {
            // 안쪽 실패를 잡고 바깥 작업을 계속 진행하려는 경우다.
        }
    }

    @Transactional
    public void orderAndFail(Runnable innerWork) {
        saveOrder();
        innerWork.run();
        throw new IllegalStateException("주문 처리 실패");
    }

    @Transactional
    public void order(Runnable innerWork) {
        saveOrder();
        innerWork.run();
    }

    private void saveOrder() {
        jdbc.update("INSERT INTO practice_order (product_code, quantity) VALUES ('CREAM', 1)");
    }
}
