CREATE TABLE MATERIAL_PURCHASES (
    id BINARY(16) NOT NULL,
    supplier_id BINARY(16) NOT NULL,
    purchase_date DATETIME(6) NOT NULL,
    total_cost DECIMAL(24,8) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    created_by VARCHAR(255) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    updated_by VARCHAR(255) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_material_purchases_supplier FOREIGN KEY (supplier_id) REFERENCES SUPPLIERS (id)
);

CREATE TABLE MATERIAL_PURCHASE_ITEMS (
    id BINARY(16) NOT NULL,
    material_purchase_id BINARY(16) NOT NULL,
    material_id BINARY(16) NOT NULL,
    quantity DECIMAL(12,4) NOT NULL,
    unit_cost DECIMAL(12,4) NOT NULL,
    subtotal DECIMAL(24,8) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    created_by VARCHAR(255) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    updated_by VARCHAR(255) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_material_purchase_items_purchase FOREIGN KEY (material_purchase_id) REFERENCES MATERIAL_PURCHASES (id),
    CONSTRAINT fk_material_purchase_items_material FOREIGN KEY (material_id) REFERENCES MATERIALS (id)
);

CREATE INDEX idx_material_purchases_supplier_date ON MATERIAL_PURCHASES (supplier_id, purchase_date);
CREATE INDEX idx_material_purchases_date ON MATERIAL_PURCHASES (purchase_date);
CREATE INDEX idx_material_purchase_items_purchase ON MATERIAL_PURCHASE_ITEMS (material_purchase_id);
CREATE INDEX idx_material_purchase_items_material ON MATERIAL_PURCHASE_ITEMS (material_id);
