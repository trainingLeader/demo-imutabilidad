package com.inmutabilidad.demo_imutabilidad.repositories;


import java.util.List;
import java.util.Optional;

import com.inmutabilidad.demo_imutabilidad.models.Product;

public interface ProductRepository {
    List<Product> findAll();
    Optional<Product> findById(Long id);
}
