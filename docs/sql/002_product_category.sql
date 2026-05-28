CREATE TABLE IF NOT EXISTS category (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Category ID',
    name VARCHAR(64) NOT NULL COMMENT 'Category name',
    sort_order INT NOT NULL DEFAULT 0 COMMENT 'Sort order',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '1 enabled, 0 disabled',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created time',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Updated time',
    PRIMARY KEY (id),
    UNIQUE KEY uk_category_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Product category';

CREATE TABLE IF NOT EXISTS product (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Product ID',
    category_id BIGINT NOT NULL COMMENT 'Category ID',
    name VARCHAR(128) NOT NULL COMMENT 'Product name',
    price DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT 'Sale price',
    stock INT NOT NULL DEFAULT 0 COMMENT 'Stock quantity',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '1 on sale, 0 off sale',
    description VARCHAR(500) NULL COMMENT 'Product description',
    image_url VARCHAR(255) NULL COMMENT 'Product image URL',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created time',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Updated time',
    PRIMARY KEY (id),
    KEY idx_product_category_id (category_id),
    KEY idx_product_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Product';

INSERT INTO category (name, sort_order, status)
VALUES
    ('数码家电', 10, 1),
    ('居家生活', 20, 1),
    ('美妆个护', 30, 1)
ON DUPLICATE KEY UPDATE
    sort_order = VALUES(sort_order),
    status = VALUES(status);
