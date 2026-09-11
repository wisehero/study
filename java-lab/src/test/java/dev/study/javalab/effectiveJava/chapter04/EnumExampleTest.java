package dev.study.javalab.effectiveJava.chapter04;

import org.junit.jupiter.api.Test;
import static dev.study.javalab.effectiveJava.chapter04.ProductStatus.*;
import static org.junit.jupiter.api.Assertions.*;

class EnumExampleTest {
    @Test
    void followsTheAllowedPathAndAcceptsRetries() {
        var product = new Product();
        for (var next : new ProductStatus[]{READY, ON_SALE, DISCONTINUED}) {
            product.changeStatus(next);
            product.changeStatus(next);
            assertEquals(next, product.status());
        }
    }

    @Test
    void rejectedChangesPreserveTheCurrentState() {
        var product = new Product();
        assertThrows(IllegalStateException.class, () -> product.changeStatus(DISCONTINUED));
        assertThrows(NullPointerException.class, () -> product.changeStatus(null));
        assertEquals(READY, product.status());
        product.changeStatus(ON_SALE);
        assertThrows(IllegalStateException.class, () -> product.changeStatus(READY));
        assertEquals(ON_SALE, product.status());
        product.changeStatus(DISCONTINUED);
        assertThrows(IllegalStateException.class, () -> product.changeStatus(ON_SALE));
        assertEquals(DISCONTINUED, product.status());
    }

    @Test
    void externalCodesAreExplicitAndUnknownValuesAreRejected() {
        for (var status : values()) assertEquals(status, fromCode(status.code()));
        assertEquals("on_sale", ON_SALE.code());
        assertThrows(IllegalArgumentException.class, () -> fromCode("UNKNOWN"));
        assertThrows(IllegalArgumentException.class, () -> fromCode("ON_SALE"));
        assertThrows(NullPointerException.class, () -> fromCode(null));
    }
}
