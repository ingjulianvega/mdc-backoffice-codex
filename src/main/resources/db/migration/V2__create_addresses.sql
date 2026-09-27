CREATE TABLE ADDRESSES (
    id BINARY(16) NOT NULL,
    street_address VARCHAR(255) NOT NULL,
    additional_info VARCHAR(255),
    city_id BINARY(16) NOT NULL,
    customer_id BINARY(16) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    created_by VARCHAR(255) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    updated_by VARCHAR(255) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_addresses_city FOREIGN KEY (city_id) REFERENCES CITIES (id)
);
CREATE INDEX idx_addresses_city_id ON ADDRESSES (city_id);
CREATE INDEX idx_addresses_customer_id ON ADDRESSES (customer_id);