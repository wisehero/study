package dev.study.commerce.catalog.api;

import dev.study.commerce.catalog.application.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.UUID;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/products")
@Validated
public class ProductController {
    private final ProductService products;

    public ProductController(ProductService products) { this.products = products; }

    @GetMapping
    public ProductPage list(@RequestParam(defaultValue = "0") @Min(0) int page,
                            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size) {
        return products.findPublicPage(page, size);
    }

    @GetMapping("/{id}")
    public ProductView get(@PathVariable UUID id) { return products.findPublic(id); }
}
