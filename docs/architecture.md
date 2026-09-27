# Arquitectura y Estándares del Backend - Tienda de Velas Artesanales "Mi Dulce Compañía"

## 1. Stack Tecnológico
- **Java:** Versión 25 (LTS)
- **Framework:** Spring Boot (última versión compatible con Java 25)
- **Documentación de API / Pruebas:** Springdoc OpenAPI / Swagger UI (`springdoc-openapi-starter-webmvc-ui`)
- **Persistencia:** Spring Data JPA / Hibernate (`JpaRepository` + `JpaSpecificationExecutor`)
- **Base de Datos:** AWS Aurora (compatible con PostgreSQL / MySQL)
- **Mapeo de Objetos:** MapStruct (`componentModel = "spring"`)
- **Validación:** Jakarta Bean Validation (`spring-boot-starter-validation`)
- **Utilidades:** Lombok

## 2. Estructura de Paquetes (Estilo John Thompson + Specifications)
com.midulcecompania.backoffice
├── config/           # Configuraciones globales (Beans, MapStruct, OpenAPI/Swagger, DataSource, etc.)
├── controller/       # Controladores REST expuestos al frontend (Anotados con OpenAPI/Swagger)
├── exception/        # Manejo global de excepciones (@ControllerAdvice y respuestas ProblemDetail)
├── model/
│   ├── dto/          # DTOs de Request y Response definidos estrictamente como Java Records (reutilizables para Angular)
│   ├── entity/       # Entidades JPA mapeadas a la base de datos AWS Aurora
│   └── enum_/        # Enumeraciones (ej. Estados de productos, estados de stock)
├── repository/       # Interfaces de Spring Data JPA (extendiendo JpaSpecificationExecutor)
├── specification/    # Clases y fábrica de Specifications (Filtros dinámicos / RSQL style)
├── service/          # Lógica de negocio (Interfaces de los servicios)
│   └── impl/         # Implementaciones de los servicios (@Service)
└── mapper/           # Interfaces de MapStruct (Mapeo DTO Records <-> Entities)

## 3. Base de Datos e Infraestructura en la Nube
- **Motor de Base de Datos:** AWS Aurora Serverless / Cluster.
- **Configuración de Persistencia:** Mapeo relacional optimizado mediante Spring Data JPA. Las propiedades de conexión, credenciales y dialectos SQL se gestionarán mediante variables de entorno seguras en el archivo `application.yml`.

## 4. Reglas de Implementación y Transaccionalidad
- **Inyección de Dependencias:** Se utiliza inyección por constructor (`@RequiredArgsConstructor` de Lombok o constructores implícitos) siguiendo las buenas prácticas defensivas recomendadas por John Thompson.
- **Gestión Transaccional:**
  - Todos los métodos de la capa de servicio (`service/impl/`) llevarán la anotación `@Transactional`.
  - Los métodos de lectura pura/consultas deberán especificar `@Transactional(readOnly = true)` para optimización de rendimiento a nivel de conexión e Hibernate.
- **DTOs Inmutables:** Todos los objetos DTO para la API REST (Requests y Responses) se implementarán utilizando **Records** de Java 25.
- **Mapeo:** Las interfaces de MapStruct estarán anotadas con `@Mapper(componentModel = "spring")` para ser gestionadas dentro del contenedor de dependencias de Spring.

## 5. Capa de Specifications y Paginación (Consultas Dinámicas)
- Los repositorios extenderán de `JpaRepository<T, ID>` y `JpaSpecificationExecutor<T>`.
- Se creará una carpeta `specification/` donde residirán los predicados reutilizables (ej. `ProductSpecifications`, `InventorySpecifications`).
- **Paginación:** Todas las búsquedas dinámicas que utilicen *Specifications* devolverán una estructura paginada mediante el uso del objeto `Pageable` de Spring Data (`Page<T>`), permitiendo una integración estandarizada con el frontend (Angular).

## 6. Documentación de API y Pruebas (OpenAPI / Swagger)
- **Interfaz Interactiva:** Se habilitará la interfaz de Swagger UI (disponible por defecto en `/swagger-ui.html` o `/swagger-ui/index.html`) para probar e interactuar con los endpoints expuestos en entorno de desarrollo.
- **Anotaciones:** Los controladores y DTOs utilizarán las anotaciones de `@Operation`, `@ApiResponse` y `@Schema` de `io.swagger.v3.oas.annotations` para documentar la semántica de las peticiones, respuestas y códigos de error (`ProblemDetail`).

## 7. Convenciones de Nombres (Estándar Oracle / Java)
- **Paquete Base:** `com.midulcecompania.backoffice`[cite: 2, 3]
- **Clases y Entidades:** `PascalCase` (ej. `ProductEntity`, `InventoryServiceImpl`, `ProductMapper`)[cite: 3].
- **Interfaces:** `PascalCase` (ej. `ProductRepository`, `ProductService`)[cite: 3].
- **Records (DTOs):** `PascalCase` (ej. `ProductRequestDTO`, `ProductResponseDTO`)[cite: 3].
- **Specifications:** Sufijo `Specification` (ej. `ProductSpecification`).
- **Métodos y Variables:** `camelCase` (ej. `hasStatus`, `stockBelowThreshold`).
- **Constantes:** `UPPER_SNAKE_CASE` (ej. `DEFAULT_PAGE_SIZE`).
- **Endpoints REST (Resources):** Plurales, en minúsculas y separados por guiones (ej. `/api/v1/products`, `/api/v1/inventory-alerts`).

## 8. Manejo de Excepciones y Validaciones (RFC 7807)
- **Manejo Centralizado:** Excepciones gestionadas a través de `@ControllerAdvice` (`GlobalExceptionHandler`).
- **Formato Estándar de Error (`ProblemDetail`):** Todos los errores devueltos por la API (4xx y 5xx) utilizarán la clase nativa `org.springframework.http.ProblemDetail` siguiendo la especificación RFC 7807.
  - Incluirá atributos estándar: `type`, `title`, `status`, `detail`, e `instance`.
  - Para errores de validación de Jakarta Bean Validation, se añadirán propiedades extendidas (`properties`) detallando los campos inválidos y sus respectivos mensajes de error.
- **Validaciones:** Anotaciones de Jakarta Bean Validation en los DTOs (Records), reflejando strictly las restricciones de nulabilidad, tamaño y longitud del esquema de la base de datos AWS Aurora.