package dev.study.javalab.collections;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CollectionExampleTest {
    @Test
    void equalValuesAreDeduplicatedInFirstAppearanceOrder() {
        var first = new ProductCode("CREAM");
        var another = new ProductCode("CREAM");
        assertNotSame(first, another);
        assertEquals(List.of(first, new ProductCode("TONER")),
                CollectionExample.uniqueCodes(List.of(first, new ProductCode("TONER"), another)));
    }

    @Test
    void changingTheInputDoesNotChangeTheReturnedSnapshot() {
        var input = new ArrayList<>(List.of(new ProductCode("CREAM")));
        var result = CollectionExample.uniqueCodes(input);
        input.clear();
        assertEquals(List.of(new ProductCode("CREAM")), result);
        assertThrows(UnsupportedOperationException.class, () -> result.add(new ProductCode("TONER")));
    }

    @Test
    void emptyInputAndInvalidCodesHaveExplicitBehavior() {
        assertEquals(List.of(), CollectionExample.uniqueCodes(List.of()));
        assertThrows(IllegalArgumentException.class, () -> new ProductCode(" "));
        assertThrows(NullPointerException.class, () -> new ProductCode(null));
    }
}
