package com.inmutabilidad.demo_imutabilidad.repositories;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import com.inmutabilidad.demo_imutabilidad.models.Product;

class ProductRepositoryImplTests {

    private final ProductRepository repository = new ProductRepositoryImpl();

    @Test
    void returnsProductByIdWithItsBasePrice() {
        assertThat(repository.findById(1L))
            .contains(new Product(1L, "Laptop", new BigDecimal("4500000.00")));
    }

    @Test
    void returnsEmptyWhenProductDoesNotExist() {
        assertThat(repository.findById(999L)).isEmpty();
    }

    @Test
    void doesNotExposeAModifiableProductCollection() {
        assertThatThrownBy(() -> repository.findAll().clear())
            .isInstanceOf(UnsupportedOperationException.class);
    }
}
