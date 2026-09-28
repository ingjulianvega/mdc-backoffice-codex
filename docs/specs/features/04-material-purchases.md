# Specification: Feature 04 - Compras de Materiales (Material Purchases)

## 1. Objetivo y Comportamiento Esperado
Implementar el registro y gestión de órdenes/facturas de compra de materiales a proveedores (`MATERIAL_PURCHASES` y `MATERIAL_PURCHASE_ITEMS`). Al registrar una compra, se debe actualizar automáticamente el stock del material correspondiente (`MATERIALS.current_stock`).

## 2. Entidades Involucradas (Modelo de Dominio)
- `MATERIAL_PURCHASES`: `id` (UUID), `supplier_id` (FK -> SUPPLIERS.id), `purchase_date` (TIMESTAMP), `total_cost` (DECIMAL/DOUBLE).
- `MATERIAL_PURCHASE_ITEMS`: `id` (UUID), `material_purchase_id` (FK -> MATERIAL_PURCHASES.id), `material_id` (FK -> MATERIALS.id), `quantity` (DECIMAL/DOUBLE), `unit_cost` (DECIMAL/DOUBLE), `subtotal` (DECIMAL/DOUBLE).

## 3. Endpoints a Implementar (`/api/v1/material-purchases`)
- `POST /api/v1/material-purchases` -> Registrar una compra con su lista de items (calcula subtotales, total y incrementa el `current_stock` de los materiales comprados).
- `GET /api/v1/material-purchases` -> Listar compras (Paginado + Specification, filtrable por `supplierId` o rango de fechas).
- `GET /api/v1/material-purchases/{id}` -> Obtener el detalle de una compra por ID (UUID) incluyendo sus items.
- `DELETE /api/v1/material-purchases/{id}` -> Anular/Eliminar una compra por ID (reinvierte/descuenta el stock ingresado previamente).

## 4. DTOs Inmutables (Java Records)
- `MaterialPurchaseItemRequestDTO` (`@NotNull UUID materialId`, `@Positive Double quantity`, `@Positive Double unitCost`)
- `MaterialPurchaseItemResponseDTO` (`UUID id`, `UUID materialId`, `String materialName`, `Double quantity`, `Double unitCost`, `Double subtotal`)
- `MaterialPurchaseRequestDTO` (`@NotNull UUID supplierId`, `Instant purchaseDate`, `@NotEmpty List<MaterialPurchaseItemRequestDTO> items`)
- `MaterialPurchaseResponseDTO` (`UUID id`, `SupplierResponseDTO supplier`, `Instant purchaseDate`, `Double totalCost`, `List<MaterialPurchaseItemResponseDTO> items`, `Instant createdAt`)

## 5. Restricciones y Reglas Técnicas
1. **Identificadores**: Usar `java.util.UUID` para todas las llaves primarias y foráneas.
2. **Lógica de Negocio en Servicio**:
    - Al crear la compra: `subtotal = quantity * unitCost` para cada item; `totalCost = suma(subtotales)`.
    - Incrementar el campo `current_stock` del `MaterialEntity` asociado a cada item.
3. **Auditoría**: Incluir atributos estándar (`createdAt`, `createdBy`, `updatedAt`, `updatedBy`).
4. **Mapeo e Inmutabilidad**: MapStruct (`@Mapper(componentModel = "spring")`) con `record` de Java 25[cite: 4].
5. **Manejo de Errores**: Retornar `ProblemDetail` (RFC 7807) ante proveedores/materiales inexistentes (`404`) o inconsistencias (`400`)[cite: 4].
6. **Transacciones**: Anotación `@Transactional` en el servicio para garantizar atomidad (si falla la actualización de stock o la compra, se revierte todo).

## 6. Estrategia de Testing y Cobertura (Sin Base de Datos Real)
- **Pruebas Unitarias (`MaterialPurchaseServiceImplTest`)**:
    - Validar cálculo correcto de subtotales y totalCost.
    - Verificar que el `current_stock` del `MaterialEntity` se actualice correctamente en el repositorio simulado (`@Mock`).
    - Lanza `ResourceNotFoundException` si el `supplierId` o algún `materialId` no existen.
- **Pruebas de Integración (`MaterialPurchaseControllerTest`)**:
    - Usar `@WebMvcTest(MaterialPurchaseController.class)` y `MockMvc`.
    - Validar HTTP 201 Created con el payload JSON de la compra y respuestas HTTP 400/404 con `ProblemDetail` (RFC 7807).

## 7. Criterios de Aceptación
- [ ] Entidades `MaterialPurchaseEntity` y `MaterialPurchaseItemEntity` mapeadas correctamente[cite: 4, 5].
- [ ] Repositorios extendiendo `JpaRepository` y `JpaSpecificationExecutor`[cite: 4].
- [ ] Endpoints expuestos bajo `/api/v1/material-purchases`[cite: 4].
- [ ] Suite completa de pruebas unitarias e integración aprobadas mediante `./mvnw clean test`.