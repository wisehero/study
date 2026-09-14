package dev.study.javalab.effectiveJava.chapter14;

import dev.study.javalab.collections.ProductCode;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EqualityExampleTest {
    @Test
    void equalCodesFollowReflexivitySymmetryAndTransitivity() {
        var first = new ProductCode("P001");
        var second = new ProductCode("P001");
        var third = new ProductCode("P001");

        assertNotSame(first, second);
        assertTrue(first.equals(first)); // 반사성: 메서드를 직접 호출한다.
        assertEquals(first, second);
        assertEquals(second, first); // 대칭성
        assertEquals(second, third);
        assertEquals(first, third);  // 추이성
        for (int i = 0; i < 3; i++) {
            assertEquals(first, second); // 상태가 같으면 비교 결과도 같다.
        }
    }

    @Test
    void differentValuesNullAndOtherTypesAreNotEqual() {
        var code = new ProductCode("P001");
        assertNotEquals(code, new ProductCode("P002"));
        assertFalse(code.equals(null));
        assertFalse(code.equals("P001"));
        assertFalse("P001".equals(code));
    }

    @Test
    void invalidCodesAreRejected() {
        assertThrows(NullPointerException.class, () -> new ProductCode(null));
        for (String value : new String[]{"", " ", "\t"}) {
            assertThrows(IllegalArgumentException.class, () -> new ProductCode(value));
        }
    }
}
