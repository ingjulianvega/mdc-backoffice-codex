# Contrato implementado: Clientes y Proveedores

La feature 03 y el modelo de dominio difieren en nombres y longitudes.
Se conservan los campos y limites de CUSTOMERS en `docs/domainModel.md`:
`documentType` (50), `documentNumber` (50), `firstName` (100),
`lastName` (100), `phoneNumber` (30, opcional) y `email` (255).
Para SUPPLIERS se conservan `companyName` (150), `contactName` (100,
opcional) y `phone` (30, opcional); se agregan `email` (150, opcional)
y `taxId` (30, obligatorio) de la especificacion.

Los emails son unicos dentro de cada recurso; tambien lo son el documento
del cliente y el NIT del proveedor. Las comprobaciones de email en servicio
ignoran mayusculas. Las actualizaciones excluyen el UUID actual.
PUT reemplaza los campos editables, incluidos los opcionales con null,
y conserva el identificador y los campos de auditoria.

- Clientes: `GET /api/v1/customers?name=&email=&documentNumber=`.
- Proveedores: `GET /api/v1/suppliers?companyName=&taxId=`.
- Ambos admiten `page`, `size` y `sort`.
- Los filtros se combinan con AND; el nombre del cliente busca en nombre
  o apellido. Las coincidencias son parciales, ignoran mayusculas y tratan
  los caracteres de comodin como texto literal. Filtros vacios se omiten.
- POST devuelve 201 y Location; GET/PUT 200; DELETE 204.
- Validaciones devuelven 400, ausencias 404 y duplicados 409 con ProblemDetail.

La migracion V4 crea ambas tablas con UUID y restricciones de unicidad.
Las pruebas nuevas utilizan repositorios simulados, mappers reales de
MapStruct y slices WebMvcTest con MockMvc, sin iniciar una base de datos.
Las pruebas preexistentes GeographyIntegrationTest y OpenApiIntegrationTest
siguen usando su configuracion H2.
