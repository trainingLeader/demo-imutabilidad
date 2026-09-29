package com.inmutabilidad.demo_imutabilidad.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import com.inmutabilidad.demo_imutabilidad.exceptions.ProductNotFoundException;
import com.inmutabilidad.demo_imutabilidad.models.Product;
import com.inmutabilidad.demo_imutabilidad.repositories.ProductRepository;
import com.inmutabilidad.demo_imutabilidad.repositories.ProductRepositoryImpl;

class ProductServiceImplTests {

    private final ProductRepository repository = new ProductRepositoryImpl();
    private final ProductService service = new ProductServiceImpl(repository);

    @Test
    void appliesPriceIncreaseToEveryListedProductWithoutChangingStoredValues() {
        assertThat(service.findAll().getFirst().price())
            .isEqualByComparingTo(new BigDecimal("6525000.00"));
        assertThat(repository.findById(1L).orElseThrow().price())
            .isEqualByComparingTo(new BigDecimal("4500000.00"));
    }

    @Test
    void appliesTheSamePriceIncreaseWhenFindingOneProduct() {
        Product product = service.findById(1L);

        assertThat(product.price()).isEqualByComparingTo(new BigDecimal("6525000.00"));
    }

    @Test
    void throwsDomainExceptionWhenProductDoesNotExist() {
        assertThatThrownBy(() -> service.findById(999L))
            .isInstanceOf(ProductNotFoundException.class)
            .hasMessage("Product with id 999 was not found");
    }
}
