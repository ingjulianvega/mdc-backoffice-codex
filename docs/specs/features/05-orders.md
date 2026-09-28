# Specification: Feature 05 - Gestión de Órdenes de Pedido (Orders)

## 1. Objetivo y Comportamiento Esperado
Implementar el registro y gestión de las órdenes de venta (`ORDERS` y `ORDER_ITEMS`). Al crear una orden se debe validar el stock de cada producto (`PRODUCTS`), descontar la cantidad vendida y calcular los valores totales.

## 2. Entidades Involucradas
- `ORDERS` y `ORDER_ITEMS` (Consultar tipos de datos, llaves primarias, foráneas y nombres exactos de columnas en `docs/domainModel.md`).

## 3. Endpoints a Implementar (`/api/v1/orders`)
- `POST /api/v1/orders` -> Crear orden (valida stock, descuenta inventario, calcula subtotales/total).
- `GET /api/v1/orders` -> Listar órdenes (Paginado + Specification por `customerId` o rango de fechas).
- `GET /api/v1/orders/{id}` -> Obtener detalle de orden por ID incluyendo sus items.
- `DELETE /api/v1/orders/{id}` -> Anular/Eliminar orden y reponer el stock descontado.

## 4. DTOs Inmutables (Java Records)
- `OrderItemRequestDTO`: `productId` (UUID), `quantity` (Integer), `unitPrice` (Long), `addressId` (UUID, opcional), `trackingNumber` (String, opcional), `carrierName` (String, opcional).
- `OrderItemResponseDTO`: `id` (UUID), `productId` (UUID), `productName` (String), `quantity` (Integer), `unitPrice` (Long), `subtotal` (Long), `addressId` (UUID), `trackingNumber` (String), `carrierName` (String).
- `OrderRequestDTO`: `customerId` (UUID), `orderDate` (Instant), `items` (List<OrderItemRequestDTO>).
- `OrderResponseDTO`: `id` (UUID), `customer` (CustomerResponseDTO), `orderDate` (Instant), `totalAmount` (Long), `items` (List<OrderItemResponseDTO>), `status` (String), `createdAt` (Instant).

## 5. Reglas Técnicas y Lógica de Negocio
1. **Modelado de Datos**: Mapear las entidades `OrderEntity` u `OrderItemEntity` respetando **estrictamente** las columnas y tipos definidos en `docs/domainModel.md` (usar `Long` para montos `BIGINT`).
2. **Cálculos**: `subtotal = quantity * unitPrice`, `totalAmount = suma(subtotales)`.
3. **Manejo de Stock**:
    - Verificar existencia de `customerId` y `productId`.
    - Validar que `product.stockQuantity >= quantity`. Si no hay suficiente, lanzar excepción (`400 Bad Request`).
    - Descontar el stock al crear y reponerlo al eliminar la orden (`DELETE`).
4. **Transacciones**: `@Transactional` en operaciones de servicio.
5. **Errores**: Mapear a `ProblemDetail` (RFC 7807).

## 6. Estrategia de Testing (JUnit 5 + Mockito + MockMvc)
- **`OrderServiceImplTest`**: Cobertura de cálculos, validación de stock insuficiente y reposición en eliminación.
- **`OrderControllerTest`**: Pruebas con `@WebMvcTest` y `MockMvc` para status HTTP (201, 200, 400, 404).