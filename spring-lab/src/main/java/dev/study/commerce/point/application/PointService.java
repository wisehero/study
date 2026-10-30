package dev.study.commerce.point.application;

import dev.study.commerce.point.domain.*;
import dev.study.commerce.point.infrastructure.*;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 거래 1건 = 트랜잭션 1개. 지갑을 맨 처음 잠그고 맨 마지막에 잔액을 바꾼다.
@Service
public class PointService {
    private final PointWalletRepository wallets;
    private final PointEarningRepository earnings;
    private final PointTransactionRepository transactions;
    private final PointTransactionDetailRepository details;

    public PointService(PointWalletRepository wallets, PointEarningRepository earnings,
                        PointTransactionRepository transactions, PointTransactionDetailRepository details) {
        this.wallets = wallets;
        this.earnings = earnings;
        this.transactions = transactions;
        this.details = details;
    }

    @Transactional
    public long earn(long memberId, long amount, EarningSource source, Long orderId, Instant expiresAt) {
        Instant baseAt = Instant.now();
        if (amount <= 0 || !expiresAt.isAfter(baseAt)) {
            throw new IllegalArgumentException("적립 금액과 만료일을 확인해 주세요.");
        }
        wallets.createIfAbsent(memberId);
        PointWallet wallet = wallets.findForUpdate(memberId).orElseThrow();

        PointTransaction earn = transactions.save(PointTransaction.earn(memberId, amount, orderId, baseAt));
        PointEarning earning = earnings.save(new PointEarning(memberId, earn.getId(), source, amount, expiresAt));
        details.save(new PointTransactionDetail(earn.getId(), earning.getId(), amount));
        wallet.add(amount);
        return earn.getId();
    }

    @Transactional
    public long use(long memberId, long orderId, long amount) {
        Instant baseAt = Instant.now();
        if (amount <= 0) {
            throw new IllegalArgumentException("사용 금액을 확인해 주세요.");
        }
        PointWallet wallet = wallets.findForUpdate(memberId)
                .orElseThrow(() -> new IllegalStateException("적립금이 부족합니다."));
        // 같은 주문의 재요청이면 처음 결과를 돌려준다. 지갑을 잠근 뒤라 같은 회원의 요청끼리는 겹치지 않는다.
        var used = transactions.findByOrderIdAndType(orderId, PointTransactionType.USE);
        if (used.isPresent()) {
            return used.get().getId();
        }
        wallet.subtract(amount);

        PointTransaction use = transactions.save(PointTransaction.use(memberId, amount, orderId, baseAt));
        long left = amount;
        for (PointEarning earning : earnings.findUsable(memberId)) {
            long taken = earning.take(left);
            details.save(new PointTransactionDetail(use.getId(), earning.getId(), -taken));
            left -= taken;
            if (left == 0) {
                return use.getId();
            }
        }
        throw new IllegalStateException("잔액과 적립 건 합계가 맞지 않습니다.");
    }

    @Transactional
    public long cancelUse(long orderId) {
        Instant baseAt = Instant.now();
        PointTransaction use = transactions.findByOrderIdAndType(orderId, PointTransactionType.USE)
                .orElseThrow(() -> new IllegalArgumentException("적립금을 사용한 주문이 없습니다."));
        PointWallet wallet = wallets.findForUpdate(use.getMemberId()).orElseThrow();
        var canceled = transactions.findByOriginTransactionId(use.getId());
        if (canceled.isPresent()) {
            return canceled.get().getId();
        }

        PointTransaction cancel = transactions.save(PointTransaction.cancel(use, baseAt));
        // 사용 상세를 보고 원래 적립 건에 되돌린다. 이미 만료된 적립 건의 처리(1부 정책 C)는 아직 하지 않는다.
        for (PointTransactionDetail usedDetail : details.findByTransactionId(use.getId())) {
            long restored = -usedDetail.getAmount();
            earnings.findById(usedDetail.getEarningId()).orElseThrow().restore(restored);
            details.save(new PointTransactionDetail(cancel.getId(), usedDetail.getEarningId(), restored));
        }
        wallet.add(cancel.getAmount());
        return cancel.getId();
    }
}
