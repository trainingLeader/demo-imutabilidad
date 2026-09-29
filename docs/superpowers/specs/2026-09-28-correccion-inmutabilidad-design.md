# Corrección de arquitectura e inmutabilidad

## Objetivo

Corregir la demostración para que los productos sean realmente inmutables, el cálculo monetario sea preciso, ambos endpoints apliquen la misma regla de precio y los productos inexistentes produzcan una respuesta HTTP 404.

## Arquitectura

Se conservará la separación actual `controller → service → repository` y las rutas públicas `GET /products` y `GET /products/{id}`.

- `Product` será un `record` con `Long id`, `String name` y `BigDecimal price`.
- `ProductRepository` será una interfaz sin anotación de Spring y retornará `Optional<Product>` para búsquedas individuales.
- `ProductRepositoryImpl` será un `@Repository` con una colección creada mediante `List.of(...)`.
- `ProductServiceImpl` será un `@Service` y tendrá una única transformación privada que multiplique el precio por `1.45`.
- `ProductController` continuará usando inyección por constructor y delegará al servicio.

## Flujo de datos

El repositorio conservará precios base inmutables. El servicio creará un nuevo `Product` con el precio incrementado para cada respuesta, sin modificar los productos almacenados. La misma transformación se usará para listados y consultas por identificador.

Los importes utilizarán `BigDecimal` construido desde cadenas. Después de multiplicar por `1.45`, el resultado tendrá escala de dos decimales y redondeo `HALF_UP`. Por ejemplo, `4500000.00` producirá `6525000.00`.

## Manejo de errores

Cuando el repositorio no encuentre un identificador, el servicio lanzará `ProductNotFoundException`. Un `@RestControllerAdvice` traducirá esa excepción a HTTP 404 mediante un `ProblemDetail` con un mensaje identificable para el consumidor.

## Dependencias

El proyecto conservará la versión administrada por el padre de Spring Boot. La dependencia directa y versionada `spring-boot-actuator:4.2.0-M1` será reemplazada por `spring-boot-starter-actuator` sin una versión explícita.

## Pruebas

La implementación seguirá ciclos de prueba primero:

- Repositorio: devuelve los productos conocidos y `Optional.empty()` para un identificador inexistente.
- Servicio: aplica exactamente el incremento del 45 %, mantiene sin cambios el valor almacenado y usa la misma regla en ambas consultas.
- Servicio: lanza `ProductNotFoundException` para un identificador inexistente.
- Web: los endpoints responden con precios transformados y una consulta inexistente devuelve 404.
- Aplicación: el contexto completo continúa arrancando.

La validación final se realizará con `./mvnw clean test`.
