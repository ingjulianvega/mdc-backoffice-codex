# Specification: Feature 06 - Velas Personalizadas de Mascotas (Pet Candles)

## 1. Objetivo y Comportamiento Esperado
Implementar el registro, personalización, gestión de fotos y trazabilidad del proceso artesanal para velas de mascotas (`PET_CANDLE` y `PET_CANDLE_PHOTOS`). Permite actualizar detalles, gestionar fotos asociadas y realizar transiciones de estado independientes por componente (base, cabeza, etiqueta, ensamble, caja, guía, empaque, transportadora), propagando el estado a `ORDER_ITEMS` y `ORDERS` al completarse.

## 2. Entidades Involucradas
- `PET_CANDLE` y `PET_CANDLE_PHOTOS` (Consultar campos, tipos de datos, llaves primarias/foráneas y nulos en `docs/domainModel.md`).

## 3. Endpoints a Implementar
### 3.1. Gestión de Velas de Mascota (`/api/v1/pet-candles`)
- `POST /api/v1/pet-candles` -> Crear registro inicial vinculado a un `order_item_id` con todos sus estados en `PENDING`.
- `GET /api/v1/pet-candles/{id}` -> Obtener detalle de la vela de mascota (incluyendo sus fotos).
- `GET /api/v1/pet-candles/by-order-item/{orderItemId}` -> Buscar registro por `orderItemId`.
- `PUT /api/v1/pet-candles/{id}` -> Actualizar datos de personalización (`petName`, `labelType`, `aromaMaterialId`, `customMessage`, `specie`, `breed`).
- `PATCH /api/v1/pet-candles/{id}/statuses` -> Actualizar individual o parcialmente los estados de las piezas (`baseStatus`, `headStatus`, `labelStatus`, `assemblyStatus`, `boxStatus`, `shippingGuideStatus`, `packagingStatus`, `carrierStatus`).

### 3.2. Fotos de Velas de Mascota (`/api/v1/pet-candle-photos`)
- `POST /api/v1/pet-candle-photos` -> Agregar URL de foto asociada a `petCandleId` (soporta flag `isPrimary`).
- `GET /api/v1/pet-candle-photos/by-pet-candle/{petCandleId}` -> Listar fotos de una vela.
- `DELETE /api/v1/pet-candle-photos/{id}` -> Eliminar una foto por ID.

## 4. DTOs Inmutables (Java Records)
- `PetCandleCreateDTO`: `orderItemId` (UUID, `@NotNull`)
- `PetCandleUpdateDTO`: `petName` (String, `@NotBlank`), `labelType` (String), `aromaMaterialId` (UUID), `customMessage` (String), `specie` (String), `breed` (String)
- `PetCandleStatusUpdateDTO`: Opción de enviar individualmente cualquiera de los estados en String (ej. `headStatus`, `baseStatus`, etc.)
- `PetCandlePhotoRequestDTO`: `petCandleId` (UUID, `@NotNull`), `photoUrl` (String, `@NotBlank`), `isPrimary` (Boolean)
- `PetCandlePhotoResponseDTO`: `id` (UUID), `petCandleId` (UUID), `photoUrl` (String), `isPrimary` (Boolean)
- `PetCandleResponseDTO`: `id` (UUID), `orderItemId` (UUID), `petName` (String), `labelType` (String), `aromaMaterialId` (UUID), `customMessage` (String), `specie` (String), `breed` (String), estados (`baseStatus`, `headStatus`, `labelStatus`, `assemblyStatus`, `boxStatus`, `shippingGuideStatus`, `packagingStatus`, `carrierStatus`), `photos` (List<PetCandlePhotoResponseDTO>)

## 5. Reglas Técnicas y Lógica de Negocio
1. **Valores por Defecto**:
    - Al crear `PET_CANDLE`, los 8 estados deben inicializarse en `PENDING`.
2. **Regla de Propagación de Estados (Cascada)**:
    - Los estados finales para completar una vela son:
        - `baseStatus`: `COMPLETED`
        - `headStatus`: `AT_WORKSHOP_ONE`
        - `labelStatus`: `AT_WORKSHOP_ONE`
        - `assemblyStatus`: `ASSEMBLED`
        - `boxStatus`: `AVAILABLE`
        - `shippingGuideStatus`: `AT_WORKSHOP_ONE`
        - `packagingStatus`: `PACKAGED`
        - `carrierStatus`: `DELIVERED`
    - Cuando en un `PATCH` todos los estados de `PET_CANDLE` alcancen su valor final, el servicio debe actualizar el `status` de la `OrderItemEntity` correspondiente a `COMPLETED`.
    - Si **todos** los `OrderItemEntity` de una `OrderEntity` están en `COMPLETED`, el `status` de la `OrderEntity` cambia automáticamente a `COMPLETED`.
3. **Transacciones**: Anotación `@Transactional` para garantizar la actualización en cascada.
4. **Manejo de Errores**: `ProblemDetail` (RFC 7807) ante IDs o estados inválidos (`400` / `404`).

## 6. Estrategia de Testing (JUnit 5 + Mockito + MockMvc)
- **`PetCandleServiceImplTest`**:
    - Verificar creación inicial con estados en `PENDING`.
    - Probar actualización de personalización (`PUT`).
    - Probar actualización de estados (`PATCH`) y verificar la activación de la regla en cascada que completa `ORDER_ITEM` y `ORDER`.
    - Probar adición de fotos (`PET_CANDLE_PHOTOS`).
- **`PetCandleControllerTest` y `PetCandlePhotoControllerTest`**: Pruebas de integración `@WebMvcTest` y `MockMvc` para validar contratos REST.