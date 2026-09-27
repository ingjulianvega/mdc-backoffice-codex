# Mi Dulce Compania — Backoffice

Backend Java 25 / Spring Boot 4.1.1. El paquete base es `com.mdc.backoffice`.

## Ejecutar localmente

Requisito: JDK 25, con `JAVA_HOME` apuntando al directorio del JDK (no a `bin`).
No hace falta instalar Maven ni MySQL para el perfil `local`.

Desde la raiz del proyecto, en PowerShell:

```powershell
# En este equipo existe este JDK; en otro equipo cambia la ruta.
$env:JAVA_HOME = "C:\Program Files\JetBrains\IntelliJ IDEA 2026.2.3\jbr"
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=local"
```

En IntelliJ tambien puedes ejecutar `BackofficeApplication` con el perfil activo `local`.
El servidor escucha en `127.0.0.1:8080`; puedes cambiar el puerto con la variable `PORT`.
Para detenerlo desde la terminal, usa Ctrl+C.

- Swagger UI: http://localhost:8080/swagger-ui/index.html
- Contrato JSON: http://localhost:8080/v3/api-docs
- Contrato YAML: http://localhost:8080/v3/api-docs.yaml

El perfil local usa **H2 en memoria**: empieza vacio y los datos se pierden al detenerlo.
Crea primero un departamento y usa su UUID para crear ciudades. La auditoria usa `system`
hasta que se incorpore autenticacion. Se permite CORS para Angular en
`http://localhost:4200` y `http://127.0.0.1:4200`.

## Probar los endpoints

En Swagger UI abre una operacion, pulsa **Try it out**, completa el cuerpo y pulsa **Execute**.
Tambien puedes usar PowerShell:

```powershell
$department = Invoke-RestMethod http://localhost:8080/api/v1/departments -Method Post -ContentType "application/json" -Body '{"name":"Antioquia"}'
$body = @{ name = "Medellin"; departmentId = $department.id } | ConvertTo-Json
$city = Invoke-RestMethod http://localhost:8080/api/v1/cities -Method Post -ContentType "application/json" -Body $body
Invoke-RestMethod "http://localhost:8080/api/v1/cities?departmentId=$($department.id)&page=0&size=20&sort=name,asc"
```

Ambos recursos exponen POST, GET paginado, GET por UUID, PUT y DELETE.
Las paginas tienen `content`, `number`, `size`, `totalElements`, `totalPages`, `first` y `last`.
`page` empieza en 0. Los errores usan `application/problem+json`; los de validacion
incluyen un mapa `errors` con campo y mensaje. El nombre es obligatorio, no puede estar
en blanco y admite hasta 100 caracteres.

## Contrato para Angular

El contrato se genera desde los controladores y records Java, no se mantiene a mano.
Incluye DTOs de entrada/salida, UUID, fechas ISO-8601, campos obligatorios, paginacion
con tipos concretos y errores. Los `operationId` son estables y determinan los nombres
de los metodos generados; los tags `Departments` y `Cities` agrupan los servicios.

Con el backend iniciado, actualiza el archivo versionable:

```powershell
.\scripts\export-openapi.ps1
```

Esto guarda `docs/api/openapi.json`. Tambien se genera `target/openapi.json` al ejecutar
`OpenApiIntegrationTest`, sin iniciar un servidor externo.

Cuando crees el proyecto Angular, instala y fija alli la version de
`@openapitools/openapi-generator-cli` y la version del generador en `openapitools.json`.
Con la CLI disponible, desde este backend puedes generar un cliente:

```powershell
openapi-generator-cli validate -i docs/api/openapi.json
openapi-generator-cli generate -i docs/api/openapi.json -g typescript-angular -c docs/api/angular-generator-config.json -o target/angular-client
```

La salida contiene modelos TypeScript y servicios HttpClient; integra el cliente
generado en el frontend. Ajusta `ngVersion` a la version del futuro proyecto Angular.
Configura `BASE_PATH` con `http://localhost:8080` en desarrollo, o usa un proxy `/api`
hacia el backend; el contrato usa un servidor relativo `/`. Habilita HttpClient en Angular.
No edites los archivos generados: regeneralos cuando cambie el contrato.

Referencia: https://openapi-generator.tech/docs/generators/typescript-angular/

## MySQL / Aurora

Sin el perfil `local`, configura `DB_URL`, `DB_USERNAME` y `DB_PASSWORD`.
Flyway crea las tablas mediante `V1__create_geography.sql` y Hibernate valida el esquema.
H2 no sustituye una verificacion contra MySQL/Aurora. Swagger y OpenAPI estan desactivados
fuera de `local`, salvo que establezcas `API_DOCS_ENABLED=true`.

## Pruebas

```powershell
.\mvnw.cmd test
```

Se usa JUnit Jupiter 6, compatible con Spring Boot 4.1.1, Mockito y MockMvc.
La suite comprueba servicios, mappers, HTTP, persistencia y el contrato OpenAPI.
Springdoc 3.1.1 corresponde a la linea compatible con Spring Boot 4: https://springdoc.org/
