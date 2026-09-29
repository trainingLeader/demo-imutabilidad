package com.inmutabilidad.demo_imutabilidad.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.springframework.stereotype.Service;

import com.inmutabilidad.demo_imutabilidad.exceptions.ProductNotFoundException;
import com.inmutabilidad.demo_imutabilidad.models.Product;
import com.inmutabilidad.demo_imutabilidad.repositories.ProductRepository;

@Service
public class ProductServiceImpl implements ProductService {
    private static final BigDecimal PRICE_INCREASE_FACTOR = new BigDecimal("1.45");

    private final ProductRepository repositoryProduct;

    public ProductServiceImpl(ProductRepository repositoryProduct) {
        this.repositoryProduct = repositoryProduct;
    }
    public List<Product> findAll(){
        return repositoryProduct.findAll().stream()
            .map(this::applyPriceIncrease)
            .toList();
    }

    public Product findById(Long id) {
        return repositoryProduct.findById(id)
            .map(this::applyPriceIncrease)
            .orElseThrow(() -> new ProductNotFoundException(id));
    }

    private Product applyPriceIncrease(Product product) {
        BigDecimal adjustedPrice = product.price()
            .multiply(PRICE_INCREASE_FACTOR)
            .setScale(2, RoundingMode.HALF_UP);

        return new Product(product.id(), product.name(), adjustedPrice);
    }
}
