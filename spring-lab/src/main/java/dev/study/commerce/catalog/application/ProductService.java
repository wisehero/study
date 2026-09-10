package dev.study.commerce.catalog.application;

import dev.study.commerce.catalog.domain.Product;
import dev.study.commerce.catalog.domain.ProductStatus;
import dev.study.commerce.catalog.infrastructure.ProductRepository;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ProductService {
    private final ProductRepository products;

    public ProductService(ProductRepository products) {
        this.products = products;
    }

    @Transactional
    public ProductView create(String sku, String name, long price, ProductStatus status) {
        // DB uniqueness is the authority, including concurrent requests.
        return ProductView.from(products.saveAndFlush(new Product(sku, name, price, status)));
    }

    public ProductView findPublic(UUID id) {
        return products.findByIdAndStatus(id, ProductStatus.ACTIVE)
                .map(ProductView::from).orElseThrow(ProductNotFoundException::new);
    }

    public ProductPage findPublicPage(int page, int size) {
        var result = products.findByStatus(ProductStatus.ACTIVE,
                PageRequest.of(page, size, Sort.by("id")));
        return new ProductPage(result.map(ProductView::from).getContent(), page, size,
                result.getTotalElements(), result.getTotalPages());
    }
}
