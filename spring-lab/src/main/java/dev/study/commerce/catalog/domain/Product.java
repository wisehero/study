package dev.study.commerce.catalog.domain;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "product")
public class Product {
    @Id
    private UUID id;
    @Column(nullable = false, length = 40, unique = true)
    private String sku;
    @Column(nullable = false, length = 120)
    private String name;
    @Column(nullable = false)
    private long price;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProductStatus status;
    @Version
    private Long version;

    protected Product() {}

    public Product(String sku, String name, long price, ProductStatus status) {
        if (sku == null || !sku.matches("[A-Z0-9-]{1,40}")) {
            throw new IllegalArgumentException("상품 코드를 확인해 주세요.");
        }
        if (name == null || name.isBlank() || name.length() > 120 || price < 0 || status == null) {
            throw new IllegalArgumentException("상품 정보를 확인해 주세요.");
        }
        this.id = UUID.randomUUID();
        this.sku = sku;
        this.name = name.strip();
        this.price = price;
        this.status = status;
    }

    public UUID getId() { return id; }
    public String getSku() { return sku; }
    public String getName() { return name; }
    public long getPrice() { return price; }
    public ProductStatus getStatus() { return status; }
    public Long getVersion() { return version; }
}
