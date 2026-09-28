# Specification: Feature 03 - Clientes y Proveedores (Customers & Suppliers)

## 1. Objetivo y Comportamiento Esperado
Implementar los componentes para administrar la información de los actores comerciales del sistema: Clientes (`CUSTOMERS`) y Proveedores (`SUPPLIERS`). Permitir el registro, consulta paginada con filtros dinámicos, actualización y eliminación de registros.

## 2. Entidades Involucradas (Modelo de Dominio)
- `CUSTOMERS`: `id` (UUID), `first_name` (VARCHAR 100), `last_name` (VARCHAR 100), `email` (VARCHAR 150, Unique), `phone` (VARCHAR 20), `document_number` (VARCHAR 30, Unique).
- `SUPPLIERS`: `id` (UUID), `company_name` (VARCHAR 150), `contact_name` (VARCHAR 100), `email` (VARCHAR 150), `phone` (VARCHAR 20), `tax_id` / `nit` (VARCHAR 30, Unique).

## 3. Endpoints a Implementar
### 3.1. Clientes (`/api/v1/customers`)
- `POST /api/v1/customers` -> Crear cliente
- `GET /api/v1/customers` -> Listar clientes (Paginado + Specification por nombre, email o documento)
- `GET /api/v1/customers/{id}` -> Obtener cliente por ID (UUID)
- `PUT /api/v1/customers/{id}` -> Actualizar cliente
- `DELETE /api/v1/customers/{id}` -> Eliminar cliente

### 3.2. Proveedores (`/api/v1/suppliers`)
- `POST /api/v1/suppliers` -> Crear proveedor
- `GET /api/v1/suppliers` -> Listar proveedores (Paginado + Specification por razón social o NIT/Tax ID)
- `GET /api/v1/suppliers/{id}` -> Obtener proveedor por ID (UUID)
- `PUT /api/v1/suppliers/{id}` -> Actualizar proveedor
- `DELETE /api/v1/suppliers/{id}` -> Eliminar proveedor

## 4. DTOs Inmutables (Java Records)
- `CustomerRequestDTO`, `CustomerResponseDTO`
- `SupplierRequestDTO`, `SupplierResponseDTO`

## 5. Restricciones y Reglas Técnicas
1. **Identificadores**: Usar `java.util.UUID` para las llaves primarias (`id`).
2. **Validaciones**: Validar correos con `@Email`, campos obligatorios con `@NotBlank` y unicidad de documentos/emails en la capa de servicio.
3. **Auditoría**: Mapear campos de auditoría estándar (`createdAt`, `createdBy`, `updatedAt`, `updatedBy`).
4. **DTOs e Inmutabilidad**: Uso estricto de `record` de Java 25 y mappers de MapStruct (`@Mapper(componentModel = "spring")`).
5. **Manejo de Errores**: Retornar `ProblemDetail` (RFC 7807) en caso de recursos no encontrados (`404`) o duplicados / errores de validación (`400` / `409`).
6. **Transacciones**: Usar `@Transactional` en operaciones de escritura y `@Transactional(readOnly = true)` en lecturas.

## 6. Estrategia de Testing y Cobertura (Sin Base de Datos Real)
- **Pruebas Unitarias (`CustomerServiceImplTest`, `SupplierServiceImplTest`)**:
    - Validar guardado exitoso y manejo de excepciones ante IDs inexistentes.
    - Verificar validación de duplicidad en email o documento mediante mocks del repositorio.
- **Pruebas de Integración (`CustomerControllerTest`, `SupplierControllerTest`)**:
    - Usar `@WebMvcTest` y `MockMvc` para validar el contrato REST, códigos HTTP (200, 201, 400, 404) y estructura RFC 7807.

## 7. Criterios de Aceptación
- [ ] Entidades `CustomerEntity` y `SupplierEntity` mapeadas correctamente.
- [ ] Repositorios extendiendo `JpaRepository` y `JpaSpecificationExecutor`.
- [ ] Mappers, Servicios y Controllers expuestos en `/api/v1/customers` y `/api/v1/suppliers`.
- [ ] Pruebas unitarias e integración ejecutadas y aprobadas mediante `./mvnw clean test`.