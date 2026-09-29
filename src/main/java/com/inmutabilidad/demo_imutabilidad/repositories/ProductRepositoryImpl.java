package com.inmutabilidad.demo_imutabilidad.repositories;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.inmutabilidad.demo_imutabilidad.models.Product;

@Repository
public class ProductRepositoryImpl implements ProductRepository{
    private final List<Product> products;

    public ProductRepositoryImpl() {
        this.products = List.of(
            new Product(1L, "Laptop", new BigDecimal("4500000.00")),
            new Product(2L, "Smartphone", new BigDecimal("2000000.00")),
            new Product(3L, "Tablet", new BigDecimal("1200000.00")),
            new Product(4L, "Monitor", new BigDecimal("800000.00")),
            new Product(5L, "Keyboard", new BigDecimal("150000.00")),
            new Product(6L, "Mouse", new BigDecimal("80000.00")),
            new Product(7L, "Printer", new BigDecimal("700000.00")),
            new Product(8L, "External Hard Drive", new BigDecimal("350000.00")),
            new Product(9L, "Headphones", new BigDecimal("200000.00")),
            new Product(10L, "Webcam", new BigDecimal("250000.00"))
        );
    }

    public List<Product> findAll() {
        return products;
    }

    public Optional<Product> findById(Long id) {
        return products.stream()
            .filter(p -> p.id().equals(id))
            .findFirst();
    }
}
