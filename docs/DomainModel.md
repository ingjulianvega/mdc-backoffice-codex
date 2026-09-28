# Modelo de Dominio - Backoffice Tienda de Velas Artesanales

Este documento define la estructura completa de persistencia (Entidades JPA) basada estrictamente en el diagrama original de la base de datos, adaptada con UUIDs, nombres de columnas limpios y campos de auditoría estándar.

## 1. Convenciones Generales
- **Nombres de Tablas:** `UPPER_SNAKE_CASE` (respetando los nombres del diagrama original).
- **Nombres de Columnas:** `snake_case`.
- **Claves Primarias (PK):** `id` (Tipo `UUID`).
- **Claves Foráneas (FK):** Sufijo `_id` (ej. `department_id`, `city_id`, `address_id`).
- **Campos de Auditoría:** `created_at`, `created_by`, `updated_at`, `updated_by`.

---

## 2. Catálogo de Entidades y Campos

### TABLA: PET_CANDLE
- `id`: `UUID` (PK, Not Null)   
- `order_item_id`: `UUID` (FK -> ORDER_ITEMS.id, Not Null)   
- `pet_name`: `VARCHAR(255)` (Not Null)   
- `label_type`: `VARCHAR`(100)` (Nullable)   
- `aroma_material_id`: `UUID` (FK -> MATERIALS.id, Nullable)   
- `custom_message`: `TEXT` (Nullable)   
- `specie`: `VARCHAR`(100)` (Nullable)   
- `breed`: `VARCHAR`(100)` (Nullable)   
- `base_status`: `VARCHAR`(50)` (Nullable) -- PENDING, COMPLETED   
- `head_status`: `VARCHAR`(50)` (Nullable) -- PENDING, WAY_HOLD, AT_WORKSHOP_TWO, PAINTED, AT_WORKSHOP_ONE   
- `label_status`: `VARCHAR`(50)` (Nullable) -- PENDING, PRINTED, AT_WORKSHOP_ONE   
- `assembly_status`: `VARCHAR`(50)` (Nullable) -- PENDING, ASSEMBLED   
- `box_status`: `VARCHAR`(50)` (Nullable) -- PENDING, AVAILABLE   
- `shipping_guide_status`: `VARCHAR`(50)` (Nullable) -- PENDING, GENERATED, PRINTED, AT_WORKSHOP_ONE   
- `packaging_status`: `VARCHAR`(50)` (Nullable) -- PENDING, PACKAGED   
- `carrier_status`: `VARCHAR`(50)` (Nullable) -- PENDING, DISPATCHED, DELIVERED, WITH_ISSUE   
- *Campos de Auditoría:* `created_at`, `created_by`, `updated_at`, `updated_by`.

### TABLA: `PET_CANDLE_PHOTOS` 
- `id`: `UUID` (PK, Not Null)
- `pet_candle_id`: `UUID` (FK -> `PET_CANDLE.id`, Not Null)
- `photo_url`: `VARCHAR(500)` (Not Null)
- `is_primary`: `BOOLEAN` (Nullable)
- *Campos de Auditoría:* `created_at`, `created_by`, `updated_at`, `updated_by`.

### TABLA: `ORDERS` 

- `id`: `UUID` (PK, Not Null)   
- `customer_id`: `UUID` (FK -> CUSTOMER.id, Not Null)   
- `order_date`: `TIMESTAMP` (Not Null)   
- `total_amount`: `BIGINT` (Not Null)  
- `status`: `VARCHAR(50)` (Not Null)
- *Campos de Auditoría:* `created_at`, `created_by`, `updated_at`, `updated_by`.

### TABLA: `CUSTOMERS`

- `id`: `UUID` (PK, Not Null)   
- `document_type`: `VARCHAR(50)` (Not Null)   
- `document_number`: `VARCHAR(50)` (Not Null)   
- `first_name`: `VARCHAR(100)` (Not Null)   
- `last_name`: `VARCHAR(100)` (Not Null)   
- `phone_number`: `VARCHAR(30)` (Nullable)   
- `email`: `VARCHAR(255)` (Not Null)   
- *Campos de Auditoría:* `created_at`, `created_by`, `updated_at`, `updated_by`.

### TABLA: ORDER_ITEMS
- `status`: `VARCHAR(50)` (Not Null) -- PENDING, COMPLETED
- `id`: `UUID` (PK, Not Null)   
- `order_id`: `UUID` (FK -> ORDERS.id, Not Null)   
- `product_id`: `UUID` (FK -> PRODUCTS.id, Not Null)   
- `quantity`: `INTEGER` (Not Null)   
- `unit_price`: `BIGINT` (Not Null)   
- `subtotal`: `BIGINT` (Not Null)   
- `address_id`: `UUID` (FK -> ADDRESSES.id, Nullable)   
- `tracking_number`: `VARCHAR(100)` (Nullable)   
- `carrier_name`: `VARCHAR(100)` (Nullable)   
- *Campos de Auditoría:* `created_at`, `created_by`, `updated_at`, `updated_by`.

### TABLA: ADDRESSES
- `id`: `UUID` (PK, Not Null)   
- `street_address`: `VARCHAR(255)` (Not Null)   
- `additional_info`: `VARCHAR(255)` (Nullable)   
- `city_id`: `UUID` (FK -> CITIES.id, Not Null)   
- `customer_id`: `UUID` (FK -> CUSTOMERS.id, Not Null)  
- *Campos de Auditoría:* `created_at`, `created_by`, `updated_at`, `updated_by`.

### TABLA: PRODUCTS
- `id`: `UUID` (PK, Not Null)   
- `name`: `VARCHAR(150)` (Not Null)   
- `description`: `TEXT` (Nullable)   
- `price`: `BIGINT` (Not Null)   
- `stock_quantity`: `INTEGER` (Not Null)   
- `min_stock_alert`: `INTEGER` (Nullable)
- *Campos de Auditoría:* `created_at`, `created_by`, `updated_at`, `updated_by`.

### TABLA: CITIES
- `id`: `UUID` (PK, Not Null)   
- `name`: `VARCHAR`(100) (Not Null)   
- `department_id`: `UUID` (FK -> DEPARTMENTS.id, Not Null) 
- *Campos de Auditoría:* `created_at`, `created_by`, `updated_at`, `updated_by`.

### TABLA: PRODUCTS_RECIPES
- `id`: `UUID` (PK, Not Null)   
- `product_id`: `UUID` (FK -> PRODUCTS.id, Not Null)   
- `material_id`: `UUID` (FK -> MATERIALS.id, Not Null)   
- `required_quantity`: `DECIMAL(12, 4)` (Not Null)  
- *Campos de Auditoría:* `created_at`, `created_by`, `updated_at`, `updated_by`. 

### TABLA: DEPARTMENTS
- `id`: `UUID` (PK, Not Null)   
- `name`: `VARCHAR(100)` (Not Null)
- *Campos de Auditoría:* `created_at`, `created_by`, `updated_at`, `updated_by`. 

### TABLA: MATERIALS
- `id`: `UUID` (PK, Not Null)   
- `name`: `VARCHAR(150)` (Not Null)   
- `unit_of_measure`: `VARCHAR(50)` (Not Null)   
- `current_stock`: `DECIMAL(12, 4)` (Not Null) 
- *Campos de Auditoría:* `created_at`, `created_by`, `updated_at`, `updated_by`.

### TABLA: MATERIAL_PURCHASES
- `id`: `UUID` (PK, Not Null)   
- `supplier_id`: `UUID` (FK -> SUPPLIERS.id, Not Null)   
- `purchase_date`: `TIMESTAMP` (Not Null)   
- `total_cost`: `BIGINT` (Not Null)
- *Campos de Auditoría:* `created_at`, `created_by`, `updated_at`, `updated_by`.     

### MATERIAL_PURCHASE_ITEMS
- `id`: `UUID` (PK, Not Null)   
- `material_purchase_id`: `UUID` (FK -> MATERIAL_PURCHASES.id, Not Null)   
- `material_id`: `UUID` (FK -> MATERIALS.id, Not Null)   
- `quantity`: `DECIMAL(12, 4)` (Not Null)   
- `unit_cost`: `BIGINT` (Not Null)   
- `subtotal`: `BIGINT` (Not Null)
- *Campos de Auditoría:* `created_at`, `created_by`, `updated_at`, `updated_by`.      

### TABLA: SUPPLIERS
- `id`: `UUID` (PK, Not Null)   
- `company_name`: `VARCHAR(150)` (Not Null)   
- `contact_name`: `VARCHAR(100)` (Nullable)   
- `email`: `VARCHAR(150)` (Nullable, Unique)
- `phone`: `VARCHAR(30)` (Nullable)
- `tax_id`: `VARCHAR(30)` (Not Null, Unique) — Campo `taxId` en `SupplierEntity`.
- *Campos de Auditoría:* `created_at`, `created_by`, `updated_at`, `updated_by`.

## 3. Catálogo de Enumeraciones (Enums)

Todas las columnas de estado (`VARCHAR`) deben mapearse a enums estrictos en el código backend (`com.mdc.backoffice.model.enum_`).

### `BaseStatus`
- `PENDING`
- `COMPLETED`

### `HeadStatus`
- `PENDING`
- `WAY_HOLD`
- `AT_WORKSHOP_TWO`
- `PAINTED`
- `AT_WORKSHOP_ONE`

### `LabelStatus`
- `PENDING`
- `PRINTED`
- `AT_WORKSHOP_ONE`

### `AssemblyStatus`
- `PENDING`
- `ASSEMBLED`

### `BoxStatus`
- `PENDING`
- `AVAILABLE`

### `ShippingGuideStatus`
- `PENDING`
- `GENERATED`
- `PRINTED`
- `AT_WORKSHOP_ONE`

### `PackagingStatus`
- `PENDING`
- `PACKAGED`

### `CarrierStatus`
- `PENDING`
- `DISPATCHED`
- `DELIVERED`
- `WITH_ISSUE`
