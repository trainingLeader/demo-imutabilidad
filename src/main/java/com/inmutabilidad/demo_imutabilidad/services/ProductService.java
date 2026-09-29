package com.inmutabilidad.demo_imutabilidad.services;

import java.util.List;

import com.inmutabilidad.demo_imutabilidad.models.Product;

public interface ProductService {
    List<Product> findAll();
    Product findById(Long id);
}
