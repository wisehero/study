package dev.study.javalab.effectiveJava.chapter02;

import dev.study.javalab.effectiveJava.chapter01.Product;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** 학습 02 · 아이템 54: 빈 결과, 실패, 수정 가능 여부를 구분한다. */
public final class ProductCatalog {
    @FunctionalInterface
    public interface ProductSource {
        /** 정상 조회는 null이 아닌 목록을 반환하고, 조회 실패는 예외로 전달한다. */
        List<Product> load(long categoryId);
    }

    private final ProductSource source;

    public ProductCatalog(ProductSource source) {
        this.source = Objects.requireNonNull(source);
    }

    /**
     * 항목 유무에 관계없이 수정 가능한 목록 복사본을 반환한다.
     * 원소 Product는 공유된다. 존재하지 않는 카테고리는 이 예제에서 예외로 구분한다.
     * 원본의 조회 예외를 빈 목록으로 바꾸지 않는다.
     */
    public List<Product> findProducts(long categoryId) {
        return new ArrayList<>(source.load(categoryId));
    }

    /** 항목 유무에 관계없이 목록 구조를 수정할 수 없는 스냅샷을 반환한다. */
    public List<Product> findReadOnlyProducts(long categoryId) {
        return List.copyOf(source.load(categoryId));
    }
}
