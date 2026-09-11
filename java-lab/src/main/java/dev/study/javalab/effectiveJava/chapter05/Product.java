package dev.study.javalab.effectiveJava.chapter05;

import java.util.ArrayList;
import java.util.List;

public final class Product {
    private int price;
    private final List<String> tags = new ArrayList<>();

    public Product(int price) { changePrice(price); }
    public int price() { return price; }

    public void changePrice(int price) {
        PriceValidator.validate(price);
        this.price = price;
    }

    public void addTag(String tag) {
        if (tag == null || tag.isBlank()) {
            throw new IllegalArgumentException("태그는 비어 있을 수 없습니다.");
        }
        tags.add(tag);
    }

    // 내부 목록 대신 수정 불가능한 복사본을 반환한다.
    public List<String> tags() { return List.copyOf(tags); }
}
