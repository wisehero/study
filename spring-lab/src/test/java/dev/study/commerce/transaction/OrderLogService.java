package dev.study.commerce.transaction;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

// 안쪽 트랜잭션 역할을 하는 실습용 서비스다. 프록시를 거치도록 별도 빈으로 둔다.
public class OrderLogService {
    private final JdbcTemplate jdbc;

    public OrderLogService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Transactional
    public TxInfo saveWithRequired() {
        return saveLog();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public TxInfo saveWithRequiresNew() {
        return saveLog();
    }

    @Transactional
    public void failWithRequired() {
        saveLog();
        throw new IllegalStateException("로그 저장 실패");
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void failWithRequiresNew() {
        saveLog();
        throw new IllegalStateException("로그 저장 실패");
    }

    @Transactional(propagation = Propagation.NESTED)
    public TxInfo saveWithNested() {
        return saveLog();
    }

    @Transactional(propagation = Propagation.NESTED)
    public void failWithNested() {
        saveLog();
        throw new IllegalStateException("로그 저장 실패");
    }

    @Transactional
    public void failWithCheckedError() throws Exception {
        saveLog();
        throw new Exception("체크 예외");
    }

    @Transactional(rollbackFor = Exception.class)
    public void failWithCheckedErrorAndRollbackFor() throws Exception {
        saveLog();
        throw new Exception("체크 예외");
    }

    // IllegalStateException에는 noRollbackFor가 더 가까운 규칙이다.
    @Transactional(rollbackFor = Exception.class, noRollbackFor = IllegalStateException.class)
    public void failWithNoRollbackFor() {
        saveLog();
        throw new IllegalStateException("로그 저장 실패");
    }

    @Transactional
    public void markRollbackOnly() {
        saveLog();
        TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
    }

    private TxInfo saveLog() {
        jdbc.update("INSERT INTO practice_order_log (message) VALUES ('주문 기록')");
        return TxInfoReader.read(jdbc);
    }
}
