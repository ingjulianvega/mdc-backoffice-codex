ALTER TABLE ORDER_ITEMS ADD COLUMN status VARCHAR(50) NOT NULL DEFAULT 'PENDING';

CREATE TABLE PET_CANDLE (
    id BINARY(16) NOT NULL PRIMARY KEY,
    order_item_id BINARY(16) NOT NULL,
    pet_name VARCHAR(255) NOT NULL,
    label_type VARCHAR(100),
    aroma_material_id BINARY(16),
    custom_message TEXT,
    specie VARCHAR(100),
    breed VARCHAR(100),
    base_status VARCHAR(50),
    head_status VARCHAR(50),
    label_status VARCHAR(50),
    assembly_status VARCHAR(50),
    box_status VARCHAR(50),
    shipping_guide_status VARCHAR(50),
    packaging_status VARCHAR(50),
    carrier_status VARCHAR(50),
    created_at DATETIME(6) NOT NULL,
    created_by VARCHAR(255) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    updated_by VARCHAR(255) NOT NULL,
    CONSTRAINT fk_pet_candle_order_item FOREIGN KEY (order_item_id) REFERENCES ORDER_ITEMS (id),
    CONSTRAINT fk_pet_candle_aroma FOREIGN KEY (aroma_material_id) REFERENCES MATERIALS (id)
);
CREATE INDEX idx_pet_candle_order_item ON PET_CANDLE (order_item_id);

CREATE TABLE PET_CANDLE_PHOTOS (
    id BINARY(16) NOT NULL PRIMARY KEY,
    pet_candle_id BINARY(16) NOT NULL,
    photo_url VARCHAR(500) NOT NULL,
    is_primary BOOLEAN,
    created_at DATETIME(6) NOT NULL,
    created_by VARCHAR(255) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    updated_by VARCHAR(255) NOT NULL,
    CONSTRAINT fk_pet_candle_photos_candle FOREIGN KEY (pet_candle_id) REFERENCES PET_CANDLE (id)
);
CREATE INDEX idx_pet_candle_photos_candle ON PET_CANDLE_PHOTOS (pet_candle_id);
