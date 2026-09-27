CREATE TABLE DEPARTMENTS (
    id BINARY(16) NOT NULL,
    name VARCHAR(100) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    created_by VARCHAR(255) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    updated_by VARCHAR(255) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE CITIES (
    id BINARY(16) NOT NULL,
    name VARCHAR(100) NOT NULL,
    department_id BINARY(16) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    created_by VARCHAR(255) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    updated_by VARCHAR(255) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_cities_department FOREIGN KEY (department_id) REFERENCES DEPARTMENTS (id)
);

CREATE INDEX idx_cities_department_id ON CITIES (department_id);
