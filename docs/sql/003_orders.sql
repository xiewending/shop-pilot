CREATE TABLE IF NOT EXISTS orders (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Order ID',
    order_no VARCHAR(32) NOT NULL COMMENT 'Order number',
    customer_name VARCHAR(64) NOT NULL COMMENT 'Customer name',
    customer_phone VARCHAR(32) NOT NULL COMMENT 'Customer phone',
    total_amount DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT 'Order total amount',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '0 pending payment, 1 pending shipment, 2 shipped, 3 completed, 4 cancelled',
    shipping_address VARCHAR(255) NOT NULL COMMENT 'Shipping address',
    remark VARCHAR(500) NULL COMMENT 'Order remark',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created time',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Updated time',
    PRIMARY KEY (id),
    UNIQUE KEY uk_orders_order_no (order_no),
    KEY idx_orders_status (status),
    KEY idx_orders_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Order';

CREATE TABLE IF NOT EXISTS order_item (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Order item ID',
    order_id BIGINT NOT NULL COMMENT 'Order ID',
    product_id BIGINT NOT NULL COMMENT 'Product ID',
    product_name VARCHAR(128) NOT NULL COMMENT 'Product name snapshot',
    unit_price DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT 'Unit price snapshot',
    quantity INT NOT NULL DEFAULT 1 COMMENT 'Quantity',
    total_price DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT 'Line total price',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created time',
    PRIMARY KEY (id),
    KEY idx_order_item_order_id (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Order item';

INSERT INTO orders (order_no, customer_name, customer_phone, total_amount, status, shipping_address, remark)
VALUES
    ('SP202605280001', '陈明', '13800000001', 299.00, 0, '上海市浦东新区软件园', '等待客户付款'),
    ('SP202605280002', '李晨', '13800000002', 188.50, 1, '杭州市西湖区文三路', '本周五前发货'),
    ('SP202605280003', '王宁', '13800000003', 520.00, 2, '深圳市南山区科技园', '物流单号已上传'),
    ('SP202605280004', '周航', '13800000004', 79.90, 3, '北京市朝阳区国贸', '样品订单已完成'),
    ('SP202605280005', '许佳', '13800000005', 99.00, 4, '广州市天河区体育西路', '客户取消订单')
ON DUPLICATE KEY UPDATE
    customer_name = VALUES(customer_name),
    customer_phone = VALUES(customer_phone),
    total_amount = VALUES(total_amount),
    status = VALUES(status),
    shipping_address = VALUES(shipping_address),
    remark = VALUES(remark);

DELETE oi
FROM order_item oi
INNER JOIN orders o ON o.id = oi.order_id
WHERE o.order_no IN (
    'SP202605280001',
    'SP202605280002',
    'SP202605280003',
    'SP202605280004',
    'SP202605280005'
);

INSERT INTO order_item (order_id, product_id, product_name, unit_price, quantity, total_price)
SELECT o.id, 1001, '无线鼠标', 99.00, 1, 99.00
FROM orders o
WHERE o.order_no = 'SP202605280001';

INSERT INTO order_item (order_id, product_id, product_name, unit_price, quantity, total_price)
SELECT o.id, 1002, '机械键盘', 200.00, 1, 200.00
FROM orders o
WHERE o.order_no = 'SP202605280001';

INSERT INTO order_item (order_id, product_id, product_name, unit_price, quantity, total_price)
SELECT o.id, 1003, '香薰机', 188.50, 1, 188.50
FROM orders o
WHERE o.order_no = 'SP202605280002';

INSERT INTO order_item (order_id, product_id, product_name, unit_price, quantity, total_price)
SELECT o.id, 1004, '智能台灯', 260.00, 2, 520.00
FROM orders o
WHERE o.order_no = 'SP202605280003';

INSERT INTO order_item (order_id, product_id, product_name, unit_price, quantity, total_price)
SELECT o.id, 1005, '纯棉毛巾套装', 79.90, 1, 79.90
FROM orders o
WHERE o.order_no = 'SP202605280004';

INSERT INTO order_item (order_id, product_id, product_name, unit_price, quantity, total_price)
SELECT o.id, 1006, '旅行保温杯', 99.00, 1, 99.00
FROM orders o
WHERE o.order_no = 'SP202605280005';
