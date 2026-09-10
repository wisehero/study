package dev.study.commerce.catalog.api;

import dev.study.commerce.catalog.domain.ProductStatus;
import jakarta.validation.constraints.*;

public record CreateProductRequest(
        @NotBlank @Pattern(regexp = "[A-Z0-9-]{1,40}") String sku,
        @NotBlank @Size(max = 120) String name,
        @NotNull @PositiveOrZero Long price,
        @NotNull ProductStatus status) {}
