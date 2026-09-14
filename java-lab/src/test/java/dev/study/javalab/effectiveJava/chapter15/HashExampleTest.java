package dev.study.javalab.effectiveJava.chapter15;

import dev.study.javalab.collections.ProductCode;
import java.util.HashMap;
import java.util.HashSet;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class HashExampleTest {
    @Test
    void equalCodesWorkAsSetElementsAndMapKeys() {
        var first = new ProductCode("P001");
        var second = new ProductCode("P001");
        assertEquals(first.hashCode(), second.hashCode());

        var codes = new HashSet<ProductCode>();
        assertTrue(codes.add(first));
        assertFalse(codes.add(second));
        assertEquals(1, codes.size());

        var stock = new HashMap<ProductCode, Integer>();
        stock.put(first, 10);
        assertEquals(10, stock.get(second));
        assertEquals(10, stock.put(second, 20));
        assertEquals(1, stock.size());
        assertEquals(20, stock.get(first));
    }

    @Test
    void differentHashesBreakLookupEvenWhenEqualsReturnsTrue() {
        // Object의 기본 해시는 서로 다르다는 보장이 없어, 실패를 재현할 값을 직접 지정한다.
        var first = new BrokenHashCode("P001", 1);
        var second = new BrokenHashCode("P001", 2);
        assertEquals(first, second);

        var codes = new HashSet<BrokenHashCode>();
        codes.add(first);
        codes.add(second);
        assertEquals(2, codes.size());

        var stock = new HashMap<BrokenHashCode, Integer>();
        stock.put(first, 10);
        assertNull(stock.get(second));
    }

    @Test
    void constantHashesStillDistinguishDifferentValues() {
        var codes = new HashSet<ConstantHashCode>();
        assertTrue(codes.add(new ConstantHashCode("P001")));
        assertTrue(codes.add(new ConstantHashCode("P002")));
        assertFalse(codes.add(new ConstantHashCode("P001")));
        assertEquals(2, codes.size()); // 충돌해도 equals로 구분한다. 성능 측정은 아니다.
    }

    @Test
    void changingAStoredKeyCanBreakLookupAndRemoval() {
        var code = new MutableCode("P001");
        var codes = new HashSet<MutableCode>();
        var stock = new HashMap<MutableCode, Integer>();
        codes.add(code);
        stock.put(code, 10);
        assertTrue(codes.contains(code));
        assertEquals(10, stock.get(code));

        code.value = "P002"; // 잘못된 사용을 재현하기 위한 가변 키

        assertFalse(codes.contains(code));
        assertFalse(codes.remove(code));
        assertNull(stock.get(code));
        assertNull(stock.remove(code));
        assertSame(code, codes.iterator().next());
        assertSame(code, stock.keySet().iterator().next()); // 객체는 여전히 저장돼 있다.
    }

    // 아래 세 타입은 문제를 재현하는 테스트 전용 예제다.
    private record BrokenHashCode(String value, int hash) {
        @Override
        public boolean equals(Object other) {
            return other instanceof BrokenHashCode that && value.equals(that.value);
        }

        @Override
        public int hashCode() { return hash; } // 의도적으로 동등성과 어긋나는 해시값
    }

    private record ConstantHashCode(String value) {
        @Override
        public int hashCode() { return 1; }
    }

    private static final class MutableCode {
        private String value;

        private MutableCode(String value) { this.value = value; }

        @Override
        public boolean equals(Object other) {
            return other instanceof MutableCode that && value.equals(that.value);
        }

        @Override
        public int hashCode() { return value.hashCode(); }
    }
}
