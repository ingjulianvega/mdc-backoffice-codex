CREATE TABLE CUSTOMERS (
    id BINARY(16) NOT NULL,
    document_type VARCHAR(50) NOT NULL,
    document_number VARCHAR(50) NOT NULL UNIQUE,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone_number VARCHAR(30),
    email VARCHAR(255) NOT NULL UNIQUE,
    created_at DATETIME(6) NOT NULL,
    created_by VARCHAR(255) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    updated_by VARCHAR(255) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE SUPPLIERS (
    id BINARY(16) NOT NULL,
    company_name VARCHAR(150) NOT NULL,
    contact_name VARCHAR(100),
    email VARCHAR(150) UNIQUE,
    phone VARCHAR(30),
    tax_id VARCHAR(30) NOT NULL UNIQUE,
    created_at DATETIME(6) NOT NULL,
    created_by VARCHAR(255) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    updated_by VARCHAR(255) NOT NULL,
    PRIMARY KEY (id)
);
