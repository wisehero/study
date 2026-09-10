package dev.study.commerce.catalog.api;

import dev.study.commerce.catalog.application.ProductService;
import dev.study.commerce.catalog.application.ProductView;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/products")
public class AdminProductController {
    private final ProductService products;

    public AdminProductController(ProductService products) { this.products = products; }

    @PostMapping
    public ResponseEntity<ProductView> create(@Valid @RequestBody CreateProductRequest request) {
        var result = products.create(request.sku(), request.name(), request.price(), request.status());
        // A draft is not publicly readable, so no public Location is advertised here.
        return ResponseEntity.status(201).body(result);
    }
}
