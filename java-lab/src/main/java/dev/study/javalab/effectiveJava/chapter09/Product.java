package dev.study.javalab.effectiveJava.chapter09;

public final class Product {
    public enum Status { READY, ON_SALE }

    private final String name;
    private final int price;
    private final Status status;

    private Product(String name, int price, Status status) {
        if (name == null || name.isBlank() || price < 0) {
            throw new IllegalArgumentException("상품명과 0 이상의 가격이 필요합니다.");
        }
        this.name = name;
        this.price = price;
        this.status = status;
    }

    public static Product ready(String name, int price) {
        return new Product(name, price, Status.READY);
    }

    public static Product onSale(String name, int price) {
        return new Product(name, price, Status.ON_SALE);
    }

    public String name() { return name; }
    public int price() { return price; }
    public Status status() { return status; }
}
