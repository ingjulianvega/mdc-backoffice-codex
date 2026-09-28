CREATE TABLE ORDERS (
    id BINARY(16) NOT NULL PRIMARY KEY,
    customer_id BINARY(16) NOT NULL,
    order_date TIMESTAMP(6) NOT NULL,
    total_amount BIGINT NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    created_by VARCHAR(255) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    updated_by VARCHAR(255) NOT NULL,
    CONSTRAINT fk_orders_customer FOREIGN KEY (customer_id) REFERENCES CUSTOMERS (id)
);
CREATE TABLE ORDER_ITEMS (
    id BINARY(16) NOT NULL PRIMARY KEY,
    order_id BINARY(16) NOT NULL,
    product_id BINARY(16) NOT NULL,
    quantity INTEGER NOT NULL,
    unit_price BIGINT NOT NULL,
    subtotal BIGINT NOT NULL,
    address_id BINARY(16),
    tracking_number VARCHAR(100),
    carrier_name VARCHAR(100),
    created_at DATETIME(6) NOT NULL,
    created_by VARCHAR(255) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    updated_by VARCHAR(255) NOT NULL,
    CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES ORDERS (id),
    CONSTRAINT fk_order_items_product FOREIGN KEY (product_id) REFERENCES PRODUCTS (id),
    CONSTRAINT fk_order_items_address FOREIGN KEY (address_id) REFERENCES ADDRESSES (id)
);
CREATE INDEX idx_orders_customer_date ON ORDERS (customer_id, order_date);
CREATE INDEX idx_orders_date ON ORDERS (order_date);
CREATE INDEX idx_order_items_order ON ORDER_ITEMS (order_id);
