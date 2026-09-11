package dev.study.javalab.effectiveJava.chapter03;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BoxingExampleTest {
    @Test
    void missingQuantityIsRejectedButExplicitZeroIsAccepted() {
        var error = assertThrows(IllegalArgumentException.class,
                () -> new StockRequest(null).requiredQuantity());
        assertEquals("수량은 필수입니다.", error.getMessage());
        assertEquals(0L, new StockRequest(0L).requiredQuantity());
        assertEquals(1L, new StockRequest(1L).requiredQuantity());
        assertThrows(IllegalArgumentException.class, () -> new StockRequest(-1L).requiredQuantity());
    }

    @Test
    void nullComparisonFailsBeforeTheConditionCanBeDecided() {
        assertThrows(NullPointerException.class, () -> BoxingExample.unsafeIsSoldOut(null));
        assertTrue(BoxingExample.unsafeIsSoldOut(0L));
        assertFalse(BoxingExample.unsafeIsSoldOut(1L));
    }

    @Test
    void nullElementFailsDuringUnboxingAndIsNotTreatedAsZero() {
        var quantities = Arrays.asList(1L, null, 3L);
        assertThrows(NullPointerException.class, () -> BoxingExample.unsafeSum(quantities));
        assertThrows(IllegalArgumentException.class, () -> BoxingExample.sumRequiredQuantities(quantities));
    }

    @Test
    void validatedSumHandlesEmptyZeroPositiveAndInvalidInputs() {
        assertEquals(0L, BoxingExample.sumRequiredQuantities(List.of()));
        assertEquals(4L, BoxingExample.sumRequiredQuantities(List.of(1L, 0L, 3L)));
        assertThrows(IllegalArgumentException.class,
                () -> BoxingExample.sumRequiredQuantities(List.of(-1L)));
        assertThrows(NullPointerException.class, () -> BoxingExample.sumRequiredQuantities(null));
        assertThrows(ArithmeticException.class,
                () -> BoxingExample.sumRequiredQuantities(List.of(Long.MAX_VALUE, 1L)));
    }

    @Test
    void cachedReferencesMustNotBeMistakenForGeneralValueComparison() {
        Integer a = 100;
        Integer b = 100;
        assertSame(a, b);
        Integer x = 1000;
        Integer y = 1000;
        // 캐시 범위 밖의 참조 동일성에 의존하지 않는다.
        assertTrue(x.equals(y));
        assertTrue(x == 1000); // int와 비교하므로 언박싱된다.
        assertFalse(x.equals(1000L)); // Integer와 Long은 서로 다른 타입이다.
    }

    @Test
    void nullSafeEqualityDoesNotMakeUnknownBusinessValuesKnown() {
        Integer unknown = null;
        assertThrows(NullPointerException.class, () -> unknown.equals(0));
        assertTrue(Objects.equals(unknown, null));
        assertFalse(Objects.equals(unknown, 0));
        assertTrue(Objects.equals(Integer.valueOf(1000), Integer.valueOf(1000)));
    }

    @Test
    void boxedAndPrimitiveAccumulatorsProduceTheSameValue() {
        assertEquals(499500L, BoxingExample.boxedTotal(1000));
        assertEquals(499500L, BoxingExample.primitiveTotal(1000));
        assertEquals(0L, BoxingExample.primitiveTotal(0));
        // 실행 시간의 우열은 단위 테스트로 단정하지 않는다.
    }
}
