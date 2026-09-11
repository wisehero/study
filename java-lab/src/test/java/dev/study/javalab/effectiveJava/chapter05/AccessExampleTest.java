package dev.study.javalab.effectiveJava.chapter05;

import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AccessExampleTest {
    @Test
    void priceChangesOnlyAfterValidation() {
        var product = new Product(1000);
        assertThrows(IllegalArgumentException.class, () -> product.changePrice(-1));
        assertEquals(1000, product.price());
        product.changePrice(0);
        assertEquals(0, product.price());
        assertThrows(IllegalArgumentException.class, () -> new Product(-1));
        // product.price = -1; // private이므로 컴파일되지 않는다.
    }

    @Test
    void returnedTagsCannotBypassValidationOrChangeInternalState() {
        var product = new Product(1000);
        var empty = product.tags();
        assertThrows(UnsupportedOperationException.class, () -> empty.add(""));
        product.addTag("sale");
        var snapshot = product.tags();
        assertThrows(UnsupportedOperationException.class, snapshot::clear);
        assertThrows(IllegalArgumentException.class, () -> product.addTag(" "));
        assertThrows(IllegalArgumentException.class, () -> product.addTag(null));
        product.addTag("new");
        assertEquals(List.of(), empty);
        assertEquals(List.of("sale"), snapshot);
        assertEquals(List.of("sale", "new"), product.tags());
    }
}
