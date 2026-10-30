package dev.study.commerce.point.infrastructure;

import dev.study.commerce.point.domain.PointEarning;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PointEarningRepository extends JpaRepository<PointEarning, Long> {
    // 만료가 임박한 것부터 쓴다. 지갑을 잠근 뒤에 읽으므로 적립 건은 따로 잠그지 않는다.
    @Query("""
            select e from PointEarning e
            where e.memberId = :memberId and e.remainingAmount > 0
            order by e.expiresAt, e.id""")
    List<PointEarning> findUsable(Long memberId);
}
