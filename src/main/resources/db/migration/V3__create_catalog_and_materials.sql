CREATE TABLE MATERIALS (
    id BINARY(16) NOT NULL,
    name VARCHAR(150) NOT NULL,
    unit_of_measure VARCHAR(50) NOT NULL,
    current_stock DECIMAL(12,4) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    created_by VARCHAR(255) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    updated_by VARCHAR(255) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE PRODUCTS (
    id BINARY(16) NOT NULL,
    name VARCHAR(150) NOT NULL,
    description TEXT,
    price BIGINT NOT NULL,
    stock_quantity INTEGER NOT NULL,
    min_stock_alert INTEGER,
    created_at DATETIME(6) NOT NULL,
    created_by VARCHAR(255) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    updated_by VARCHAR(255) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE PRODUCTS_RECIPES (
    id BINARY(16) NOT NULL,
    product_id BINARY(16) NOT NULL,
    material_id BINARY(16) NOT NULL,
    required_quantity DECIMAL(12,4) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    created_by VARCHAR(255) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    updated_by VARCHAR(255) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_product_recipes_product FOREIGN KEY (product_id) REFERENCES PRODUCTS (id),
    CONSTRAINT fk_product_recipes_material FOREIGN KEY (material_id) REFERENCES MATERIALS (id)
);

CREATE INDEX idx_product_recipes_product ON PRODUCTS_RECIPES (product_id);
CREATE INDEX idx_product_recipes_material ON PRODUCTS_RECIPES (material_id);

