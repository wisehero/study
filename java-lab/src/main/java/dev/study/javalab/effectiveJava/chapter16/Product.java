package dev.study.javalab.effectiveJava.chapter16;

import java.util.List;

public final class Product {
    private final List<String> tags;

    public Product(List<String> tags) {
        this.tags = List.copyOf(tags);
    }

    public List<String> tags() { return tags; }
}
