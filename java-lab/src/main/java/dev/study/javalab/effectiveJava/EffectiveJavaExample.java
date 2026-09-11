package dev.study.javalab.effectiveJava;

import dev.study.javalab.effectiveJava.chapter01.Product;
import dev.study.javalab.effectiveJava.chapter02.ProductCatalog;
import java.time.LocalDateTime;
import java.util.List;

public class EffectiveJavaExample {
    public static void main(String[] args) {
        dev.study.javalab.effectiveJava.chapter03.BoxingExample.main(args);
        var product = new Product(1000,
                LocalDateTime.of(2026, 9, 10, 0, 0),
                LocalDateTime.of(2026, 9, 20, 0, 0));
        try {
            product.changePrice(-1);
        } catch (IllegalArgumentException e) {
            System.out.println("01 잘못된 가격 거절: " + e.getMessage());
        }
        System.out.println("01 실패 후 기존 가격: " + product.price());

        var catalog = new ProductCatalog(id -> {
            if (id == 1) return List.of(product);
            if (id == 2) return List.of();
            throw new IllegalArgumentException("카테고리가 없습니다.");
        });
        var empty = catalog.findProducts(2);
        System.out.println("02 정상 조회의 빈 결과: " + empty);
        empty.add(product);
        System.out.println("02 빈 결과에도 추가 가능: " + empty.size());
        System.out.println("02 원본 조회 결과는 유지: " + catalog.findProducts(2).size());
    }
}
