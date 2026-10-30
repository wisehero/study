package dev.study.commerce.point.infrastructure;

import dev.study.commerce.point.domain.PointTransaction;
import dev.study.commerce.point.domain.PointTransactionType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PointTransactionRepository extends JpaRepository<PointTransaction, Long> {
    Optional<PointTransaction> findByPaymentAttemptIdAndType(UUID paymentAttemptId, PointTransactionType type);
    Optional<PointTransaction> findByOriginTransactionId(Long originTransactionId);
}
