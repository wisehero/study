package dev.study.javalab.effectiveJava.chapter16;

public final class Price {
    private final int amount;

    public Price(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("가격은 0 이상이어야 합니다.");
        }
        this.amount = amount;
    }

    public Price discount(int discountAmount) {
        if (discountAmount < 0 || discountAmount > amount) {
            throw new IllegalArgumentException("할인 금액은 0 이상 가격 이하여야 합니다.");
        }
        return new Price(amount - discountAmount);
    }

    public int amount() { return amount; }
}
