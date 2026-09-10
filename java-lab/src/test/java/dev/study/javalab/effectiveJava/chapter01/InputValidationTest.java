package dev.study.javalab.effectiveJava.chapter01;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class InputValidationTest {
    private final LocalDateTime start = LocalDateTime.of(2026, 9, 10, 0, 0);
    private final LocalDateTime end = start.plusDays(10);

    @Test
    void priceBoundariesAndFailurePreserveState() {
        var product = new Product(1000, start, end);
        assertThrows(IllegalArgumentException.class, () -> product.changePrice(-1));
        assertEquals(1000, product.price());
        for (int price : new int[]{0, 1}) {
            product.changePrice(price);
            assertEquals(price, product.price());
        }
    }

    @Test
    void invalidPeriodsPreserveBothFields() {
        var product = new Product(1000, start, end);
        var newStart = start.plusDays(5);
        for (var invalidEnd : new LocalDateTime[]{newStart.minusDays(1), newStart}) {
            assertThrows(IllegalArgumentException.class,
                    () -> product.changeDisplayPeriod(newStart, invalidEnd));
            assertPeriod(product, start, end);
        }
        assertThrows(NullPointerException.class, () -> product.changeDisplayPeriod(null, end));
        assertPeriod(product, start, end);
        assertThrows(NullPointerException.class, () -> product.changeDisplayPeriod(newStart, null));
        assertPeriod(product, start, end);
        product.changeDisplayPeriod(newStart, newStart.plusDays(1));
        assertPeriod(product, newStart, newStart.plusDays(1));
    }

    @Test
    void constructorAlsoEnforcesRules() {
        assertThrows(IllegalArgumentException.class, () -> new Product(-1, start, end));
        assertThrows(IllegalArgumentException.class, () -> new Product(0, end, start));
    }

    @Test
    void pageSizeChecksBothBoundaries() {
        for (int value : new int[]{-1, 0, 101}) {
            assertThrows(IllegalArgumentException.class, () -> new PageSize(value));
        }
        for (int value : new int[]{1, 100}) {
            assertEquals(value, new PageSize(value).value());
        }
    }

    private void assertPeriod(Product product, LocalDateTime start, LocalDateTime end) {
        assertEquals(start, product.start());
        assertEquals(end, product.end());
    }
}
