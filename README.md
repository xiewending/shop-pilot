# ShopPilot

ShopPilot 是一个电商运营后台展示项目。当前已完成项目骨架、登录权限、商品管理和订单管理模块。

## 技术栈

后端：

- Java 17
- Spring Boot 3
- Maven
- MySQL 8
- MyBatis-Plus
- Redis
- JWT
- Lombok
- Spring Validation

前端：

- Vue 3
- Vite
- TypeScript
- Element Plus
- Pinia
- Axios
- Vue Router

## 项目结构

```text
shop-pilot/
|-- backend/
|-- frontend/
|-- docs/
`-- README.md
```

## 环境准备

请先确认本机已安装：

- JDK 17
- Maven 3.8+
- Node.js 18+
- MySQL 8
- Redis

后端数据库配置在 `backend/src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/shop_pilot
    username: ${SHOP_PILOT_DB_USERNAME:root}
    password: ${SHOP_PILOT_DB_PASSWORD:}
```

本地开发可创建不提交的 `backend/src/main/resources/application-local.yml` 覆盖数据库账号：

```yaml
spring:
  datasource:
    username: your_mysql_user
    password: your_mysql_password
```

## 数据库初始化

```sql
CREATE DATABASE shop_pilot DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

然后依次执行：

```text
docs/sql/001_sys_user.sql
docs/sql/002_product_category.sql
docs/sql/003_orders.sql
```

默认登录账号：

```text
admin / admin123
```

## 启动后端

```bash
cd shop-pilot/backend
mvn spring-boot:run
```

后端地址：`http://localhost:8080`

健康检查：

```bash
curl http://localhost:8080/api/health
```

## 启动前端

```bash
cd shop-pilot/frontend
npm install
npm run dev
```

前端地址：`http://localhost:5173`

开发环境下，Vite 会把 `/api` 请求代理到 `http://localhost:8080`。

## 登录接口

```text
POST /api/auth/login
```

示例：

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"admin\",\"password\":\"admin123\"}"
```

登录成功后，受保护接口需要携带 JWT：

```text
Authorization: Bearer <token>
```

## 商品接口

商品接口均需要登录。

分类：

```text
GET /api/categories/options
```

商品：

```text
GET    /api/products                 商品分页查询，支持 page、size、keyword、categoryId、status。
GET    /api/products/{id}            商品详情。
POST   /api/products                 新增商品。
PUT    /api/products/{id}            编辑商品。
PATCH  /api/products/{id}/status     修改上下架状态，status: 1 上架，0 下架。
DELETE /api/products/{id}            删除商品。
```

## 订单接口

订单接口均需要登录。

```text
GET   /api/orders/status-options     订单状态选项。
GET   /api/orders                    订单分页查询，支持 page、size、keyword、status。
GET   /api/orders/{id}               订单详情，包含订单明细。
PATCH /api/orders/{id}/status        修改订单状态。
```

订单状态：

```text
0 待付款
1 待发货
2 已发货
3 已完成
4 已取消
```

允许的状态流转：

```text
待付款   -> 待发货、已取消
待发货   -> 已发货、已取消
已发货   -> 已完成
已完成   -> 终态
已取消   -> 终态
```

## 测试商品管理

1. 启动 MySQL 和 Redis。
2. 创建 `shop_pilot` 数据库。
3. 执行 `docs/sql/001_sys_user.sql`。
4. 执行 `docs/sql/002_product_category.sql`。
5. 启动后端。
6. 启动前端。
7. 打开 `http://localhost:5173`。
8. 使用 `admin / admin123` 登录。
9. 点击 `商品管理`。
10. 测试新增、编辑、删除、搜索、分页、上下架。

## 测试订单管理

1. 启动 MySQL 和 Redis。
2. 创建 `shop_pilot` 数据库。
3. 执行 `docs/sql/001_sys_user.sql`。
4. 执行 `docs/sql/003_orders.sql`。
5. 启动后端。
6. 启动前端。
7. 打开 `http://localhost:5173`。
8. 使用 `admin / admin123` 登录。
9. 点击 `订单管理`。
10. 测试搜索、状态筛选、分页、查看详情，并按允许的状态流转修改订单状态。
