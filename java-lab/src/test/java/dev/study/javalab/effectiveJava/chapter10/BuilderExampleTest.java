package dev.study.javalab.effectiveJava.chapter10;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BuilderExampleTest {
    @Test
    void optionalStockDefaultsToZeroAndCanBeSpecified() {
        var product = new Product.Builder("크림", 10000).build();
        assertEquals("크림", product.name());
        assertEquals(10000, product.price());
        assertEquals(0, product.stock());
        assertEquals(100, new Product.Builder("크림", 10000).stock(100).build().stock());
        assertEquals(0, new Product.Builder("샘플", 0).build().price());
    }

    @Test
    void buildRejectsInvalidSettingsBeforeReturningAProduct() {
        for (String name : new String[]{null, "", " "}) {
            assertThrows(IllegalArgumentException.class, () -> new Product.Builder(name, 0).build());
        }
        assertThrows(IllegalArgumentException.class, () -> new Product.Builder("크림", -1).build());
        var builder = new Product.Builder("크림", 10000).stock(-1);
        assertThrows(IllegalArgumentException.class, builder::build);
        assertEquals(1, builder.stock(1).build().stock());
    }

    @Test
    void changingTheBuilderDoesNotChangeAnAlreadyBuiltProduct() {
        var builder = new Product.Builder("크림", 10000).stock(10);
        var first = builder.build();
        var second = builder.stock(20).build();
        assertEquals(10, first.stock());
        assertEquals(20, second.stock());
    }
}
