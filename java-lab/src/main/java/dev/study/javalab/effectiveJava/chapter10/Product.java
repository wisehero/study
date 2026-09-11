package dev.study.javalab.effectiveJava.chapter10;

public final class Product {
    private final String name;
    private final int price;
    private final int stock;

    private Product(Builder builder) {
        if (builder.name == null || builder.name.isBlank()
                || builder.price < 0 || builder.stock < 0) {
            throw new IllegalArgumentException("상품명과 0 이상의 가격·재고가 필요합니다.");
        }
        name = builder.name;
        price = builder.price;
        stock = builder.stock;
    }

    public String name() { return name; }
    public int price() { return price; }
    public int stock() { return stock; }

    public static final class Builder {
        private final String name;
        private final int price;
        private int stock = 0; // 선택 값: 생략하면 재고 없음으로 정한다.

        public Builder(String name, int price) {
            this.name = name;
            this.price = price;
        }

        public Builder stock(int stock) {
            this.stock = stock;
            return this;
        }

        public Product build() { return new Product(this); }
    }
}
