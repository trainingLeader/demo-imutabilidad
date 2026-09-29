# Corrección de inmutabilidad Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Corregir la API para representar productos inmutables con importes precisos, aplicar uniformemente el incremento del 45 % y responder 404 cuando un producto no exista.

**Architecture:** Se conserva el flujo `controller → service → repository`. El repositorio mantiene valores base inmutables, el servicio genera nuevos productos con el precio transformado y un manejador web traduce la excepción de producto inexistente a `ProblemDetail` con estado 404.

**Tech Stack:** Java 25, Spring Boot 4.1.1, Spring MVC, JUnit 5, AssertJ y MockMvc.

## Global Constraints

- Conservar `GET /products` y `GET /products/{id}`.
- Representar `price` mediante `BigDecimal` con escala 2 y redondeo `HALF_UP` al aplicar el factor `1.45`.
- No exponer ni modificar el estado almacenado por el repositorio.
- Mantener las versiones de componentes Spring Boot administradas por el padre `4.1.1`.
- No inicializar Git ni realizar commits porque el directorio actual no es un repositorio.

---

### Task 1: Modelo y repositorio inmutables

**Files:**
- Modify: `src/main/java/com/inmutabilidad/demo_imutabilidad/models/Product.java`
- Modify: `src/main/java/com/inmutabilidad/demo_imutabilidad/repositories/ProductRepository.java`
- Modify: `src/main/java/com/inmutabilidad/demo_imutabilidad/repositories/ProductRepositoryImpl.java`
- Create: `src/test/java/com/inmutabilidad/demo_imutabilidad/repositories/ProductRepositoryImplTests.java`

**Interfaces:**
- Produces: `record Product(Long id, String name, BigDecimal price)`.
- Produces: `List<Product> ProductRepository.findAll()`.
- Produces: `Optional<Product> ProductRepository.findById(Long id)`.

- [ ] **Step 1: Write the failing repository tests**

Crear pruebas con el repositorio real que verifiquen `findById(1L)` igual a `new Product(1L, "Laptop", new BigDecimal("4500000.00"))`, `findById(999L)` vacío y que `findAll()` sea una colección no modificable.

- [ ] **Step 2: Run the repository test and verify RED**

Run: `./mvnw -Dtest=ProductRepositoryImplTests test`

Expected: fallo de compilación porque `Product` todavía recibe `Long price` y `findById` todavía devuelve `Product`.

- [ ] **Step 3: Implement the immutable model and repository**

Reemplazar `Product` con:

```java
public record Product(Long id, String name, BigDecimal price) { }
```

Cambiar `findById` a `Optional<Product>`, quitar `@Repository` de la interfaz, anotar la implementación con `@Repository`, declarar `private final List<Product> products` y construirla mediante `List.of(...)` con precios `new BigDecimal("... .00")` sin espacios.

- [ ] **Step 4: Run the repository test and verify GREEN**

Run: `./mvnw -Dtest=ProductRepositoryImplTests test`

Expected: 3 pruebas, 0 fallos y 0 errores.

### Task 2: Regla de precio y producto inexistente

**Files:**
- Create: `src/main/java/com/inmutabilidad/demo_imutabilidad/exceptions/ProductNotFoundException.java`
- Modify: `src/main/java/com/inmutabilidad/demo_imutabilidad/services/ProductServiceImpl.java`
- Create: `src/test/java/com/inmutabilidad/demo_imutabilidad/services/ProductServiceImplTests.java`

**Interfaces:**
- Consumes: `Optional<Product> ProductRepository.findById(Long id)`.
- Produces: `ProductNotFoundException(Long id)`.
- Preserves: `List<Product> ProductService.findAll()` and `Product ProductService.findById(Long id)`.

- [ ] **Step 1: Write failing service tests**

Usar `ProductRepositoryImpl` real. Verificar que `findAll()` y `findById(1L)` retornan precio `6525000.00`, que una segunda lectura directa del repositorio conserva `4500000.00` y que `findById(999L)` lanza `ProductNotFoundException`.

- [ ] **Step 2: Run the service test and verify RED**

Run: `./mvnw -Dtest=ProductServiceImplTests test`

Expected: fallo de compilación por el acceso antiguo `getPrice()`/`setPrice()` y por la ausencia de `ProductNotFoundException`.

- [ ] **Step 3: Implement the price transformation and exception**

Crear una excepción de ejecución con mensaje `Product with id <id> was not found`. Anotar el servicio con `@Service` y centralizar la transformación:

```java
private Product applyPriceIncrease(Product product) {
    BigDecimal adjustedPrice = product.price()
        .multiply(new BigDecimal("1.45"))
        .setScale(2, RoundingMode.HALF_UP);
    return new Product(product.id(), product.name(), adjustedPrice);
}
```

Aplicar el método tanto en `findAll()` como después de `orElseThrow(...)` en `findById()`.

- [ ] **Step 4: Run repository and service tests and verify GREEN**

Run: `./mvnw -Dtest=ProductRepositoryImplTests,ProductServiceImplTests test`

Expected: todas las pruebas seleccionadas pasan.

### Task 3: Contrato HTTP y respuesta 404

**Files:**
- Create: `src/main/java/com/inmutabilidad/demo_imutabilidad/exceptions/GlobalExceptionHandler.java`
- Create: `src/test/java/com/inmutabilidad/demo_imutabilidad/controller/ProductControllerTests.java`

**Interfaces:**
- Consumes: `ProductNotFoundException`.
- Produces: respuesta RFC 9457 `ProblemDetail` con estado 404 y detalle proveniente de la excepción.

- [ ] **Step 1: Write failing HTTP tests**

Con `@SpringBootTest` y `@AutoConfigureMockMvc`, verificar que `/products/1` responde 200 con `price` numérico igual a `6525000.00`, `/products` incluye ese mismo valor para el primer elemento y `/products/999` responde 404 con `detail` igual a `Product with id 999 was not found`.

- [ ] **Step 2: Run the controller test and verify RED**

Run: `./mvnw -Dtest=ProductControllerTests test`

Expected: las respuestas exitosas funcionan, pero la consulta inexistente devuelve 500 porque aún no existe el manejador.

- [ ] **Step 3: Implement the exception handler**

Crear un `@RestControllerAdvice` con un método `@ExceptionHandler(ProductNotFoundException.class)` que retorne `ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage())`.

- [ ] **Step 4: Run the controller test and verify GREEN**

Run: `./mvnw -Dtest=ProductControllerTests test`

Expected: 3 pruebas, 0 fallos y 0 errores.

### Task 4: Alinear Actuator y verificar el proyecto completo

**Files:**
- Modify: `pom.xml`

**Interfaces:**
- Produces: Actuator administrado íntegramente por Spring Boot `4.1.1`.

- [ ] **Step 1: Replace the incompatible dependency**

Reemplazar `spring-boot-actuator` con versión `4.2.0-M1` por `spring-boot-starter-actuator` sin etiqueta `<version>` ni `<scope>`.

- [ ] **Step 2: Verify dependency convergence**

Run: `./mvnw dependency:tree '-Dincludes=org.springframework.boot:*'`

Expected: todos los artefactos de Spring Boot muestran versión `4.1.1` y no aparece `4.2.0-M1`.

- [ ] **Step 3: Run the complete clean test suite**

Run: `./mvnw clean test`

Expected: compilación exitosa y todas las pruebas pasan sin fallos ni errores.

- [ ] **Step 4: Inspect the final changes**

Ejecutar `find src docs -type f | sort` y revisar todos los archivos modificados. Confirmar que no quedan usos de `clone`, setters, `getPrice`, la versión `4.2.0-M1` ni colecciones mutables expuestas.
