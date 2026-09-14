package dev.study.javalab.effectiveJava.chapter16;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ImmutableExampleTest {
    @Test
    void discountReturnsANewPriceAndKeepsTheOriginal() {
        var original = new Price(10000);
        var discounted = original.discount(1000);

        assertNotSame(original, discounted);
        assertEquals(10000, original.amount());
        assertEquals(9000, discounted.amount());
        assertEquals(10000, original.discount(0).amount());
        assertEquals(0, original.discount(10000).amount());
        assertEquals(0, new Price(0).discount(0).amount());
    }

    @Test
    void invalidDiscountsLeaveTheOriginalUnchanged() {
        assertThrows(IllegalArgumentException.class, () -> new Price(-1));
        var price = new Price(10000);
        assertThrows(IllegalArgumentException.class, () -> price.discount(-1));
        assertThrows(IllegalArgumentException.class, () -> price.discount(10001));
        assertEquals(10000, price.amount());
    }

    @Test
    void productProtectsItsListOnInputAndOutput() {
        var source = new ArrayList<>(List.of("신상품"));
        var product = new Product(source);
        source.set(0, "할인");
        source.add("추천");
        source.clear();

        assertEquals(List.of("신상품"), product.tags());
        assertThrows(UnsupportedOperationException.class, () -> product.tags().add("추천"));
        assertThrows(UnsupportedOperationException.class, () -> product.tags().set(0, "할인"));
        assertThrows(UnsupportedOperationException.class, () -> product.tags().clear());
        assertEquals(List.of("신상품"), product.tags());
    }

    @Test
    void productAcceptsEmptyTagsButRejectsNulls() {
        assertEquals(List.of(), new Product(List.of()).tags());
        assertThrows(NullPointerException.class, () -> new Product(null));
        assertThrows(NullPointerException.class, () -> new Product(Arrays.asList("신상품", null)));
    }

    @Test
    void recordAloneSharesTheListAndItsElements() {
        var tag = new Tag("신상품");
        var source = new ArrayList<>(List.of(tag));
        var holder = new Tags(source);

        assertSame(source, holder.values()); // 그림 1: 목록 L1까지 공유
        tag.name = "할인";
        assertEquals("할인", holder.values().get(0).name);
        source.clear();
        assertTrue(holder.values().isEmpty());
    }

    @Test
    void copyOfSeparatesTheListButSharesMutableElements() {
        var tag = new Tag("신상품");
        var source = new ArrayList<>(List.of(tag));
        var copy = List.copyOf(source);

        assertNotSame(source, copy);
        assertSame(source.get(0), copy.get(0)); // 그림 2: T1은 같은 객체
        tag.name = "할인";
        source.clear();
        assertEquals(1, copy.size());
        assertEquals("할인", copy.get(0).name);
        assertThrows(UnsupportedOperationException.class, copy::clear);

        copy.get(0).name = "추천"; // 목록 수정 금지와 원소 수정 금지는 다르다.
        assertEquals("추천", tag.name);
    }

    @Test
    void copyingElementsSeparatesThemButDoesNotMakeThemImmutable() {
        var tag = new Tag("신상품");
        var source = new ArrayList<>(List.of(tag));
        var copy = source.stream().map(t -> new Tag(t.name)).toList();

        assertNotSame(tag, copy.get(0)); // 그림 3: T1과 T2가 분리됨
        tag.name = "할인";
        assertEquals("신상품", copy.get(0).name);
        assertThrows(UnsupportedOperationException.class, copy::clear);

        copy.get(0).name = "추천"; // T2를 노출하면 외부에서 변경할 수 있다.
        assertEquals("추천", copy.get(0).name);
        assertEquals("할인", tag.name);
    }

    // 그림의 가변 원소와, record의 얕은 불변성을 관찰하는 테스트 전용 타입이다.
    private static final class Tag {
        private String name;
        private Tag(String name) { this.name = name; }
    }

    private record Tags(List<Tag> values) {}
}
