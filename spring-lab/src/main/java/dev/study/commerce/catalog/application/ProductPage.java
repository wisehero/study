package dev.study.commerce.catalog.application;

import java.util.List;

public record ProductPage(List<ProductView> items, int page, int size,
                          long totalElements, int totalPages) {}
