package dev.study.commerce.point.infrastructure;

import dev.study.commerce.point.domain.PointWallet;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.*;

public interface PointWalletRepository extends JpaRepository<PointWallet, Long> {
    // 첫 적립이 동시에 와도 지갑은 하나만 만든다.
    @Modifying
    @Query(value = "INSERT INTO point_wallet (member_id, balance) VALUES (:memberId, 0) ON CONFLICT DO NOTHING",
            nativeQuery = true)
    void createIfAbsent(Long memberId);

    // SELECT ... FOR UPDATE: 같은 회원의 다른 거래는 이 트랜잭션이 끝날 때까지 기다린다.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select w from PointWallet w where w.memberId = :memberId")
    Optional<PointWallet> findForUpdate(Long memberId);
}
