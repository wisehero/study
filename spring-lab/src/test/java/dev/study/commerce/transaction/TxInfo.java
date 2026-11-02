package dev.study.commerce.transaction;

// PostgreSQL이 알려 주는 현재 DB 연결 번호와 트랜잭션 번호다.
public record TxInfo(int connectionId, long transactionId) {
}
