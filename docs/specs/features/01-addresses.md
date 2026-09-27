# Specification: Feature 01 - Gestión de Direcciones (Addresses)

## 1. Objetivo y Comportamiento Esperado
Implementar las operaciones CRUD para la gestión de direcciones de clientes (`ADDRESSES`). Este módulo se relaciona directamente con la entidad de ciudades (`CITIES`) mediante su `city_id` (UUID) e incluye la referencia de `customer_id` (UUID) de forma desacoplada en el modelo Java (sin relación JPA `@ManyToOne` explícita por ahora).

## 2. Entidades Involucradas (Modelo de Dominio)
- `ADDRESSES`
  - `id`: `UUID` (PK, Not Null)
  - `street_address`: `VARCHAR(255)` (Not Null)
  - `additional_info`: `VARCHAR(255)` (Nullable)
  - `city_id`: `UUID` (FK -> `CITIES.id`, Not Null)
  - `customer_id`: `UUID` (FK -> `CUSTOMERS.id`, Not Null)
  - Auditoría: `created_at`, `created_by`, `updated_at`, `updated_by`

## 3. Endpoints a Implementar (`/api/v1/addresses`)
- `POST /api/v1/addresses` -> Crear dirección asociada a una ciudad (`cityId`) y a un cliente (`customerId`).
- `GET /api/v1/addresses` -> Listar direcciones (Paginado + Specification, filtrable opcionalmente por `cityId` o `customerId`).
- `GET /api/v1/addresses/{id}` -> Obtener dirección por ID (`UUID`).
- `PUT /api/v1/addresses/{id}` -> Actualizar datos de la dirección (`streetAddress`, `additionalInfo`, `cityId`, `customerId`).
- `DELETE /api/v1/addresses/{id}` -> Eliminar dirección por ID (`UUID`).

## 4. DTOs Inmutables (Java Records)
- `AddressRequestDTO`
  - `@NotBlank String streetAddress`
  - `String additionalInfo`
  - `@NotNull UUID cityId`
  - `@NotNull UUID customerId`
- `AddressResponseDTO`
  - `UUID id`
  - `String streetAddress`
  - `String additionalInfo`
  - `CityResponseDTO city`
  - `UUID customerId`
  - `Instant createdAt`
  - `Instant updatedAt`

## 5. Restricciones y Reglas Técnicas
1. **Tipos de Datos PK/FK**: Usar `java.util.UUID` para `id`, `cityId` y `customerId`.
2. **Relación JPA con Ciudades**: Mapeo `@ManyToOne(fetch = FetchType.LAZY)` hacia `CityEntity` mapeado a la columna `city_id`.
3. **Referencia a Clientes**: Mantener el campo `customerId` como un atributo escalar tipo `UUID` mapeado a `customer_id` (`@Column(name = "customer_id", nullable = false)`), sin relación `@ManyToOne`.
4. **Atributos de Auditoría**: Herencia o inclusión de campos estándar (`createdAt`, `createdBy`, `updatedAt`, `updatedBy`).
5. **DTOs e Inmutabilidad**: Mapeo bidireccional mediante MapStruct con `record` de Java 25.
6. **Manejo de Errores**: Retornar `ProblemDetail` (RFC 7807) si la dirección o la ciudad asociada no existen (`404 Not Found`), o ante errores de validación (`400 Bad Request`).
7. **Transacciones**: Anotación `@Transactional` en operaciones de escritura/modificación y `@Transactional(readOnly = true)` en lecturas.

## 6. Estrategia de Testing y Cobertura (Sin Base de Datos Real)
- **Pruebas Unitarias (`AddressServiceImplTest`)**:
  - Verificar creación exitosa cuando la `CityEntity` existe en el repositorio simulado (`@Mock`).
  - Lanzar `ResourceNotFoundException` cuando el `cityId` (`UUID`) no existe.
  - Validar que los mappers conserven de forma precisa el `UUID` de `customerId` y las transformaciones hacia `AddressResponseDTO`.
- **Pruebas de Integración (`AddressControllerTest`)**:
  - Utilizar `@WebMvcTest(AddressController.class)` con `MockMvc`.
  - Simular respuestas de éxito HTTP 201 Created y HTTP 200 OK.
  - Validar HTTP 400 Bad Request con payload `ProblemDetail` cuando `streetAddress`, `cityId` o `customerId` sean nulos/vacíos.
  - Validar HTTP 404 Not Found al consultar o modificar un `UUID` inexistente.

## 7. Criterios de Aceptación**
- [ ] Entidad `AddressEntity` creada con los nombres exactos de columnas (`street_address`, `additional_info`, `city_id`, `customer_id`) y tipos `UUID`.
- [ ] `AddressRepository` extendiendo `JpaRepository<AddressEntity, UUID>` y `JpaSpecificationExecutor<AddressEntity>`.
- [ ] Mappers de MapStruct, Servicios e Interfaces REST expuestos en `/api/v1/addresses`.
- [ ] Suite completa de pruebas unitarias e integración ejecutadas y aprobadas mediante `./mvnw clean test`.