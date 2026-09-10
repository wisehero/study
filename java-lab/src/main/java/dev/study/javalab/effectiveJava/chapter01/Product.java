package dev.study.javalab.effectiveJava.chapter01;

import java.time.LocalDateTime;
import java.util.Objects;

/** 학습 01 · 아이템 49: 모든 입력을 검증한 뒤 상태를 변경한다. */
public final class Product {
    private int price;
    private LocalDateTime start;
    private LocalDateTime end;

    public Product(int price, LocalDateTime start, LocalDateTime end) {
        changePrice(price);
        changeDisplayPeriod(start, end);
    }

    /** 0원은 허용한다. 음수는 기존 가격을 변경하지 않고 거절한다. */
    public void changePrice(int price) {
        if (price < 0) {
            throw new IllegalArgumentException("가격은 0 이상이어야 합니다.");
        }
        this.price = price;
    }

    /** 두 날짜는 필수이며 종료일은 시작일보다 뒤여야 한다. */
    public void changeDisplayPeriod(LocalDateTime start, LocalDateTime end) {
        Objects.requireNonNull(start, "노출 시작일은 필수입니다.");
        Objects.requireNonNull(end, "노출 종료일은 필수입니다.");
        if (!end.isAfter(start)) {
            throw new IllegalArgumentException("노출 종료일은 시작일보다 뒤여야 합니다.");
        }
        // 관계 검증까지 끝낸 후 대입한다. 스레드 간 원자성을 보장하는 코드는 아니다.
        this.start = start;
        this.end = end;
    }

    public int price() { return price; }
    public LocalDateTime start() { return start; }
    public LocalDateTime end() { return end; }
}
