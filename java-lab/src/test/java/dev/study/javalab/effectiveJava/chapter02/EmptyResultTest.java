package dev.study.javalab.effectiveJava.chapter02;

import dev.study.javalab.effectiveJava.chapter01.Product;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EmptyResultTest {
    private Product product() {
        var start = LocalDateTime.of(2026, 9, 10, 0, 0);
        return new Product(1000, start, start.plusDays(1));
    }

    @Test
    void emptyAndNonEmptyResultsAreBothMutableAndDetached() {
        for (boolean populated : new boolean[]{false, true}) {
            var original = new ArrayList<Product>();
            if (populated) original.add(product());
            var catalog = new ProductCatalog(id -> original);
            var result = catalog.findProducts(1);
            assertEquals(original, result);
            assertEquals(populated ? 1 : 0, result.stream().count());
            result.add(product());
            result.clear();
            assertEquals(populated ? 1 : 0, original.size());
        }
    }

    @Test
    void readOnlyResultsRejectChangesRegardlessOfSizeAndAreSnapshots() {
        for (boolean populated : new boolean[]{false, true}) {
            var original = new ArrayList<Product>();
            if (populated) original.add(product());
            var result = new ProductCatalog(id -> original).findReadOnlyProducts(1);
            assertThrows(UnsupportedOperationException.class, () -> result.add(product()));
            original.add(product());
            assertEquals(populated ? 1 : 0, result.size());
        }
    }

    @Test
    void copyingTheListStillSharesMutableProducts() {
        var product = product();
        var result = new ProductCatalog(id -> List.of(product)).findProducts(1);
        result.getFirst().changePrice(2000);
        assertEquals(2000, product.price());
    }

    @Test
    void failureIsNotDisguisedAsAnEmptyResult() {
        var failure = new IllegalStateException("조회 연결 실패를 모의한다.");
        var catalog = new ProductCatalog(id -> { throw failure; });
        assertSame(failure, assertThrows(IllegalStateException.class, () -> catalog.findProducts(1)));
        assertSame(failure, assertThrows(IllegalStateException.class, () -> catalog.findReadOnlyProducts(1)));
    }

    @Test
    void missingCategoryIsDistinctFromExistingEmptyCategory() {
        var catalog = new ProductCatalog(id -> {
            if (id != 1) throw new IllegalArgumentException("카테고리가 없습니다.");
            return List.of();
        });
        assertTrue(catalog.findProducts(1).isEmpty());
        assertThrows(IllegalArgumentException.class, () -> catalog.findProducts(2));
    }

    @Test
    void sourceContractViolationIsNotConvertedToEmpty() {
        var catalog = new ProductCatalog(id -> null);
        assertThrows(NullPointerException.class, () -> catalog.findProducts(1));
    }
}
