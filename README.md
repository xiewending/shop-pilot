# ShopPilot

ShopPilot 是一个基于 Java + Spring Boot + Vue 3 的电商运营后台展示项目。当前已完成项目骨架、登录权限、RBAC 菜单权限、商品管理、订单管理、Redis 缓存和文件上传模块。

当前项目目录：

```text
F:\Project_by_codex\shop-pilot
```

## 技术栈

后端：

- Java 17
- Spring Boot 3.3.5
- Maven
- MySQL 8
- MyBatis-Plus 3.5.9
- Redis
- Spring Cache
- RedisTemplate
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
|   |-- pom.xml
|   `-- src/
|-- frontend/
|   |-- package.json
|   |-- vite.config.ts
|   `-- src/
|-- docs/
|   `-- sql/
|       |-- 001_sys_user.sql
|       |-- 002_product_category.sql
|       |-- 003_orders.sql
|       |-- 004_rbac.sql
|       `-- 005_product_image.sql
|-- uploads/
|-- .gitignore
`-- README.md
```

## 环境准备

请确认本机已安装：

- JDK 17
- Maven 3.8+
- Node.js 18+
- MySQL 8
- Redis

## Redis 安装和启动

推荐方式一：Docker 启动 Redis

```bash
docker run --name shop-pilot-redis -p 6379:6379 -d redis:7
```

停止 Redis：

```bash
docker stop shop-pilot-redis
```

再次启动：

```bash
docker start shop-pilot-redis
```

推荐方式二：Windows 使用 WSL 安装 Redis

```bash
sudo apt update
sudo apt install redis-server
sudo service redis-server start
redis-cli ping
```

如果返回：

```text
PONG
```

说明 Redis 已启动。

## 后端配置

后端配置文件：

```text
backend/src/main/resources/application.yml
```

默认端口：

```text
http://localhost:8080
```

默认数据库和 Redis 配置：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/shop_pilot?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
    username: ${SHOP_PILOT_DB_USERNAME:root}
    password: ${SHOP_PILOT_DB_PASSWORD:}
  data:
    redis:
      host: localhost
      port: 6379
```

本地开发可用 `backend/src/main/resources/application-local.yml` 覆盖数据库账号密码。

## 数据库初始化

先创建数据库：

```sql
CREATE DATABASE shop_pilot DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

然后按顺序执行：

```text
docs/sql/001_sys_user.sql
docs/sql/002_product_category.sql
docs/sql/003_orders.sql
docs/sql/004_rbac.sql
docs/sql/005_product_image.sql
```

如果是新库，`002_product_category.sql` 已包含商品图片字段；如果是已有库，请执行 `005_product_image.sql` 给 `product` 表补充 `image_url` 字段。

RBAC 初始化账号：

```text
admin / admin123      系统管理员，拥有商品、订单和全部按钮权限
operator / admin123   订单运营，只能看到工作台和订单管理
```

## 启动后端

```bash
cd F:\Project_by_codex\shop-pilot\backend
mvn spring-boot:run
```

健康检查：

```bash
curl http://localhost:8080/api/health
```

## 启动前端

```bash
cd F:\Project_by_codex\shop-pilot\frontend
npm install
npm run dev
```

前端地址：

```text
http://localhost:5173
```

开发环境下，Vite 会把 `/api` 和 `/uploads` 请求代理到 `http://localhost:8080`。

## Redis 缓存设计

已启用 Spring Cache：

```text
backend/src/main/java/com/shoppilot/config/RedisCacheConfig.java
```

缓存内容：

```text
product:detail      商品详情缓存，过期时间 15 分钟
product:hot:list    热门商品列表缓存，过期时间 60 秒
product:hot:rank    热门商品访问排行榜，Redis ZSet，过期时间 7 天
login:token:*       登录 token，过期时间与 JWT 一致，默认 7200 秒
```

缓存删除逻辑：

- 新增商品：清理热门商品列表缓存。
- 编辑商品：删除对应商品详情缓存，清理热门商品列表缓存。
- 商品上下架：删除对应商品详情缓存，清理热门商品列表缓存。
- 删除商品：删除对应商品详情缓存，清理热门商品列表缓存，并从热门榜 ZSet 移除。
- 退出登录：删除 Redis 中的登录 token。

## 登录与 RBAC

登录接口：

```text
POST /api/auth/login
```

退出登录接口：

```text
POST /api/auth/logout
```

登录成功后返回：

- JWT token
- 用户信息
- 角色编码列表
- 权限标识列表
- 菜单树

请求示例：

```bash
curl -X POST http://localhost:8080/api/auth/login ^
  -H "Content-Type: application/json" ^
  -d "{\"username\":\"admin\",\"password\":\"admin123\"}"
```

受保护接口需要携带：

```text
Authorization: Bearer <token>
```

## RBAC 表

```text
sys_user        用户表
sys_role        角色表
sys_menu        菜单和按钮权限表
sys_user_role   用户角色关联表
sys_role_menu   角色菜单关联表
```

## 商品接口

商品接口均需要登录。

```text
GET    /api/categories/options       商品分类选项
GET    /api/products                 商品分页查询
GET    /api/products/hot             热门商品排行榜，支持 limit 参数
GET    /api/products/{id}            商品详情，会写入商品详情缓存并增加热门榜分数
POST   /api/products                 新增商品
PUT    /api/products/{id}            编辑商品
PATCH  /api/products/{id}/status     修改上下架状态，status: 1 上架，0 下架
DELETE /api/products/{id}            删除商品
```

## 文件上传

商品图片上传接口需要登录。

```text
POST /api/upload/product-image       商品图片上传，multipart/form-data，字段名 file
GET  /uploads/**                     图片静态访问
```

上传限制：

```text
保存目录：F:\Project_by_codex\shop-pilot\uploads
最大大小：2MB
允许类型：JPG、PNG、WEBP、GIF
返回字段：url，例如 /uploads/product/20260528/xxx.png
```

## 订单接口

订单接口均需要登录。

```text
GET   /api/orders/status-options     订单状态选项
GET   /api/orders                    订单分页查询
GET   /api/orders/{id}               订单详情，包含订单明细
PATCH /api/orders/{id}/status        修改订单状态
```

订单状态：

```text
0 待付款
1 待发货
2 已发货
3 已完成
4 已取消
```

## 测试流程

1. 启动 MySQL。
2. 启动 Redis，确认 `redis-cli ping` 返回 `PONG`。
3. 创建 `shop_pilot` 数据库。
4. 依次执行 `docs/sql/001_sys_user.sql`、`002_product_category.sql`、`003_orders.sql`、`004_rbac.sql`、`005_product_image.sql`。
5. 启动后端：`cd F:\Project_by_codex\shop-pilot\backend && mvn spring-boot:run`。
6. 启动前端：`cd F:\Project_by_codex\shop-pilot\frontend && npm run dev`。
7. 打开 `http://localhost:5173`。
8. 使用 `admin / admin123` 登录。
9. 进入商品详情，多刷新几次，然后查看工作台热门商品排行榜。
10. 编辑、上下架或删除商品，确认商品详情缓存和热门榜列表缓存会被清理。
11. 进入商品管理，新增或编辑商品，上传一张 JPG/PNG/WEBP/GIF 图片，保存后确认商品列表显示图片。
12. 退出登录后，原 token 会从 Redis 删除，再访问受保护接口会返回未登录。

## 常用命令

后端测试：

```bash
cd F:\Project_by_codex\shop-pilot\backend
mvn test
```

前端构建：

```bash
cd F:\Project_by_codex\shop-pilot\frontend
npm run build
```

前端预览：

```bash
cd F:\Project_by_codex\shop-pilot\frontend
npm run preview
```
