package dev.study.javalab.effectiveJava.chapter09;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FactoryExampleTest {
    @Test
    void namesExpressDifferentCreationIntentsWithTheSameArguments() {
        var ready = Product.ready("크림", 10000);
        var onSale = Product.onSale("크림", 10000);
        assertEquals(ready.name(), onSale.name());
        assertEquals(ready.price(), onSale.price());
        assertEquals(Product.Status.READY, ready.status());
        assertEquals(Product.Status.ON_SALE, onSale.status());
    }

    @Test
    void bothFactoriesEnforceTheSameInputRules() {
        for (String name : new String[]{null, "", " "}) {
            assertThrows(IllegalArgumentException.class, () -> Product.ready(name, 0));
            assertThrows(IllegalArgumentException.class, () -> Product.onSale(name, 0));
        }
        assertThrows(IllegalArgumentException.class, () -> Product.ready("크림", -1));
        assertThrows(IllegalArgumentException.class, () -> Product.onSale("크림", -1));
        assertEquals(0, Product.ready("샘플", 0).price());
        assertEquals(0, Product.onSale("샘플", 0).price());
    }

    @Test
    void aFactoryCanReuseAnExistingObject() {
        assertSame(Boolean.TRUE, Boolean.valueOf(true));
        assertSame(Boolean.FALSE, Boolean.valueOf(false));
    }
}
