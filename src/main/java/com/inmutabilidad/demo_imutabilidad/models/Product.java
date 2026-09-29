package com.inmutabilidad.demo_imutabilidad.models;

import java.math.BigDecimal;

public record Product(Long id, String name, BigDecimal price) { }
