package dev.study.commerce.point.infrastructure;

import dev.study.commerce.point.domain.PointTransactionDetail;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PointTransactionDetailRepository extends JpaRepository<PointTransactionDetail, Long> {
    List<PointTransactionDetail> findByTransactionId(Long transactionId);
}
