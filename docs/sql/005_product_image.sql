SET @product_image_column_exists := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'product'
      AND COLUMN_NAME = 'image_url'
);

SET @product_image_sql := IF(
    @product_image_column_exists = 0,
    'ALTER TABLE product ADD COLUMN image_url VARCHAR(255) NULL COMMENT ''Product image URL'' AFTER description',
    'SELECT ''product.image_url already exists'''
);

PREPARE product_image_stmt FROM @product_image_sql;
EXECUTE product_image_stmt;
DEALLOCATE PREPARE product_image_stmt;
