package dev.study.commerce.catalog.application;

import dev.study.commerce.catalog.domain.Product;
import dev.study.commerce.catalog.domain.ProductStatus;
import java.util.UUID;

public record ProductView(UUID id, String sku, String name, long price,
                          ProductStatus status, long version) {
    public static ProductView from(Product product) {
        return new ProductView(product.getId(), product.getSku(), product.getName(),
                product.getPrice(), product.getStatus(), product.getVersion());
    }
}
