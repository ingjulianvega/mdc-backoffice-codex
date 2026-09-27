# Specification: Feature 00 - Gestión de Geografía (Departamentos y Ciudades)

## 1. Objetivo y Comportamiento Esperado
Implementar los servicios y controladores REST para administrar la información geográfica base (Departamentos y Ciudades). Este módulo sirve como soporte para la gestión de direcciones de clientes y proveedores.

## 2. Entidades Involucradas (Modelo de Dominio)
- `DEPARTMENTS` (`id`, `name`)
- `CITIES` (`id`, `name`, `department_id`)

## 3. Endpoints a Implementar
### 3.1. Departamentos (`/api/v1/departments`)
- `POST /api/v1/departments` -> Crear departamento
- `GET /api/v1/departments` -> Listar departamentos (Paginado + Specification)
- `GET /api/v1/departments/{id}` -> Obtener departamento por ID
- `PUT /api/v1/departments/{id}` -> Actualizar departamento
- `DELETE /api/v1/departments/{id}` -> Eliminar departamento

### 3.2. Ciudades (`/api/v1/cities`)
- `POST /api/v1/cities` -> Crear ciudad asignada a un departamento
- `GET /api/v1/cities` -> Listar ciudades (Paginado + filtrable opcionalmente por `departmentId`)
- `GET /api/v1/cities/{id}` -> Obtener ciudad por ID
- `PUT /api/v1/cities/{id}` -> Actualizar ciudad
- `DELETE /api/v1/cities/{id}` -> Eliminar ciudad

## 4. DTOs Inmutables (Java Records)
- `DepartmentRequestDTO`, `DepartmentResponseDTO`
- `CityRequestDTO`, `CityResponseDTO`

## 5. Restricciones y Reglas Técnicas
1. **Atributos de Auditoría**: Incluir campos de auditoría estándar (`createdAt`, `createdBy`, `updatedAt`, `updatedBy`).
2. **DTOs**: Estructurados mediante `record` de Java 25.
3. **Mapeo**: MapStruct configurado con `@Mapper(componentModel = "spring")`.
4. **Manejo de Errores**: Retornar `ProblemDetail` (RFC 7807) en caso de entidad no encontrada o error de validación.
5. **Transacciones**: Definir `@Transactional` en los servicios de modificación y `@Transactional(readOnly = true)` para lecturas.

## 6. Criterios de Aceptación
- [ ] Entidades JPA `DepartmentEntity` y `CityEntity` mapeadas correctamente.
- [ ] Repositorios extendiendo `JpaRepository` y `JpaSpecificationExecutor`.
- [ ] Mappers de MapStruct para transformar entre DTOs y Entidades.
- [ ] Endpoints REST expuestos bajo `/api/v1/departments` y `/api/v1/cities`.
- [ ] Paginación y filtros operativos en las consultas de listado.
## 7. Estrategia de Testing y Cobertura
- **Pruebas Unitarias (JUnit 5 + Mockito)**:
    - **Servicios (`DepartmentServiceImpl`, `CityServiceImpl`)**:
        - Verificar lógica de negocio aislando el repositorio con `@Mock`.
        - Validar que se lance la excepción correspondiente al buscar un ID inexistente.
        - Validar transformaciones del Mapper.
    - **Mappers (MapStruct)**:
        - Verificar la correcta conversión bidireccional entre Entidad y DTO (Record).

- **Pruebas de Integración / Controladores (`@WebMvcTest` o `@SpringBootTest`)**:
    - **Controladores (`DepartmentController`, `CityController`)**:
        - Probar endpoints REST simulando peticiones HTTP con `MockMvc`.
        - Validar respuestas de éxito (`200 OK`, `201 Created`).
        - Validar respuestas de error en formato `ProblemDetail` (RFC 7807) ante entradas inválidas (`400 Bad Request`) o recursos no encontrados (`404 Not Found`).

## 8. Casos de Prueba Mínimos (Matriz de Pruebas)
| Componente | Caso de Prueba | Resultado Esperado |
| :--- | :--- | :--- |
| `DepartmentService` | Guardar un departamento válido | Llama a `repository.save()` y retorna `DepartmentResponseDTO` |
| `DepartmentService` | Buscar por ID inexistente | Lanza `ResourceNotFoundException` |
| `CityService` | Crear ciudad con `departmentId` inexistente | Lanza `ResourceNotFoundException` y no guarda |
| `DepartmentController` | `GET /api/v1/departments/{id}` existente | HTTP 200 con JSON del departamento |
| `DepartmentController` | `POST /api/v1/departments` con nombre nulo/vacío | HTTP 400 Bad Request con `ProblemDetail` |