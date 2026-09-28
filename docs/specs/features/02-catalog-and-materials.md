# Specification: Feature 02 - Productos, Materiales y Recetas

## 1. Objetivo y Comportamiento Esperado
Implementar las operaciones CRUD para los productos de velas artesanales (`PRODUCTS`), insumos/materiales (`MATERIALS`) y la relación receta que vincula cuánto material requiere cada producto (`PRODUCTS_RECIPES`).

## 2. Entidades Involucradas (Modelo de Dominio)
- `MATERIALS`: `id` (UUID), `name` (VARCHAR 255), `unit_of_measure` (VARCHAR 50), `current_stock` (DECIMAL/DOUBLE).
- `PRODUCTS`: `id` (UUID), `name` (VARCHAR 255), `description` (TEXT), `price` (DECIMAL/DOUBLE), `stock_quantity` (INTEGER), `min_stock_alert` (INTEGER).
- `PRODUCTS_RECIPES`: `id` (UUID), `product_id` (FK -> PRODUCTS.id), `material_id` (FK -> MATERIALS.id), `required_quantity` (DECIMAL/DOUBLE).

## 3. Endpoints a Implementar
### 3.1. Materiales (`/api/v1/materials`)
- `POST /api/v1/materials` -> Crear material
- `GET /api/v1/materials` -> Listar materiales (Paginado + Specification)
- `GET /api/v1/materials/{id}` -> Obtener material por ID (UUID)
- `PUT /api/v1/materials/{id}` -> Actualizar material
- `DELETE /api/v1/materials/{id}` -> Eliminar material

### 3.2. Productos (`/api/v1/products`)
- `POST /api/v1/products` -> Crear producto
- `GET /api/v1/products` -> Listar productos (Paginado + Specification)
- `GET /api/v1/products/{id}` -> Obtener producto por ID (UUID)
- `PUT /api/v1/products/{id}` -> Actualizar producto
- `DELETE /api/v1/products/{id}` -> Eliminar producto

### 3.3. Recetas (`/api/v1/product-recipes`)
- `POST /api/v1/product-recipes` -> Asignar insumo y cantidad a un producto
- `GET /api/v1/product-recipes` -> Listar recetas (Paginado + filtrable por `productId` o `materialId`)
- `GET /api/v1/product-recipes/{id}` -> Obtener detalle por ID (UUID)
- `PUT /api/v1/product-recipes/{id}` -> Actualizar cantidad requerida
- `DELETE /api/v1/product-recipes/{id}` -> Eliminar relación de la receta

## 4. DTOs Inmutables (Java Records)
- `MaterialRequestDTO`, `MaterialResponseDTO`
- `ProductRequestDTO`, `ProductResponseDTO`
- `ProductRecipeRequestDTO`, `ProductRecipeResponseDTO`

## 5. Restricciones y Reglas Técnicas
1. **Tipos de Datos PK/FK**: Utilizar `java.util.UUID` para todos los identificadores.
2. **Relaciones JPA**: `@ManyToOne` en `ProductRecipeEntity` hacia `ProductEntity` y `MaterialEntity`.
3. **Atributos de Auditoría**: Campos estándar (`createdAt`, `createdBy`, `updatedAt`, `updatedBy`).
4. **Validaciones**: Validar precios y cantidades positivas (`@Positive`, `@Min(0)`).
5. **Manejo de Errores**: Retornar `ProblemDetail` (RFC 7807) ante IDs inexistentes (`404`) o errores de validación (`400`).
6. **Transacciones**: `@Transactional` para escrituras y `@Transactional(readOnly = true)` para lecturas.

## 6. Estrategia de Testing y Cobertura (Sin Base de Datos Real)
- **Pruebas Unitarias (`MaterialServiceImplTest`, `ProductServiceImplTest`, `ProductRecipeServiceImplTest`)**:
    - Validar lógica de negocio, búsquedas por ID y lanzamientos de `ResourceNotFoundException`.
    - Probar que no se pueda asociar un `materialId` o `productId` inexistente a una receta.
- **Pruebas de Integración (`MaterialControllerTest`, `ProductControllerTest`, `ProductRecipeControllerTest`)**:
    - Utilizar `@WebMvcTest` y `MockMvc` para validar contratos JSON, estados HTTP (200, 201, 400, 404) y respuestas de error RFC 7807.

## 7. Criterios de Aceptación
- [ ] Entidades `MaterialEntity`, `ProductEntity` y `ProductRecipeEntity` creadas y mapeadas.
- [ ] Repositorios extendiendo `JpaRepository` y `JpaSpecificationExecutor`.
- [ ] Endpoints expuestos correctamente bajo `/api/v1/`.
- [ ] Pruebas unitarias e integración ejecutadas e hiperprobadas con `./mvnw clean test`.