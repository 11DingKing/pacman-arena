# 吃豆人游戏系统 (Pac-Man Game System)

一个完整的H5吃豆人游戏系统，包含用户端游戏、排行榜、道具商城、支付功能以及管理后台。

## Quick Start

### 一键启动（推荐）

只需一条命令，自动完成 MySQL 初始化、后端构建、前端部署：

```bash
docker-compose up --build -d
```

**启动流程**（自动按依赖顺序执行）：

| 顺序 | 服务 | 说明 |
|------|------|------|
| 1 | MySQL | 数据库启动 + schema 自动初始化 |
| 2 | Backend | 等待 MySQL 健康检查通过后启动 |
| 3 | Frontend | 用户端 + 管理后台同时启动 |

**访问地址**：

| 服务 | 地址 |
|------|------|
| 用户端 | http://localhost:8081 |
| 管理后台 | http://localhost:8082 |
| 后端 API | http://localhost:8080 |
| API 文档 | http://localhost:8080/swagger-ui.html |

**测试账号**：`admin / admin123`

**常用命令**：

```bash
# 查看启动日志
docker-compose logs -f

# 停止所有服务
docker-compose down

# 重置数据库（清除数据重新初始化）
docker-compose down -v && docker-compose up --build -d
```

### 本地开发环境

**只需要 Docker，无需安装 Java/Maven！**

#### 方式一：使用便捷脚本（推荐）

```bash
# 启动开发环境
./scripts/dev-start.sh

# 停止开发环境
./scripts/dev-stop.sh
```

#### 方式二：手动启动

```bash
# 1. 启动 MySQL + 后端 (使用主 docker-compose 仅启动数据库)
docker-compose up -d mysql

# 2. 等待 MySQL 就绪后，启动后端
docker-compose up -d backend

# 3. 等待后端就绪 (约30秒)
# 检查后端是否启动: curl http://localhost:8080/api/item/list

# 4. 启动用户端前端 (新终端)
cd frontend-user
npm install && npm run dev

# 5. 启动管理后台前端 (新终端)
cd frontend-admin
npm install && npm run dev
```

#### 本地开发访问地址

| 服务     | 地址                  |
| -------- | --------------------- |
| 用户端   | http://localhost:5173 |
| 管理后台 | http://localhost:5174 |
| 后端 API | http://localhost:8080 |

#### 停止开发环境

```bash
docker-compose down
```

#### 重置数据库

```bash
docker-compose down -v
docker-compose up -d --build
```

#### 后端热重载与重启

- 后端已接入 **spring-boot-devtools**：本地用 `mvn spring-boot:run` 或 IDE 运行且开启「编译后自动构建」时，修改 Java/配置后**重新编译**会触发 **快速重启**（比冷启动更快），无需每次手动停进程再起。
- 修改 `application.yml`、环境变量、依赖（pom）等后，仍需**完整重启**一次后端；**Docker 跑后端**时，改代码后需 `docker-compose -f docker-compose.dev.yml up -d --build backend` 重建并重启容器。
- 若仍出现「系统繁忙」等异常，优先**重启后端**以加载最新代码与配置，再重试支付流程。

## 题目内容

帮我开发一个前端用vue,后端用springboot来实现的H5版本的吃豆人游戏系统,后台管理可以查看每个人的得分,用户也可以查看排名,可以使用支付宝等支付工具来购买道具

## 功能特性

### 用户端 (H5)

- 🎮 经典吃豆人游戏
- 🏆 全球排行榜
- 🛒 道具商城
- 💳 支付宝支付
- 👤 个人中心
- 📱 移动端优化（全屏游戏、可拖拽浮动按钮）

### 管理后台

- 📊 数据概览
- 👥 用户管理
- 🎯 游戏记录查看
- 🎁 道具管理
- 📋 订单管理

## 道具购买流程：模拟与真实双模式

支持**模拟**（演示、联调）与**真实**（跳转支付宝）两种模式，由配置 `ALIPAY_MOCK` 与支付宝密钥决定；**默认模拟**，无需配置即可完成演示流程。

### 流程步骤

| 步骤           | 说明                 | 模拟环境                                                                     | 真实环境                                                                                                                                                           |
| -------------- | -------------------- | ---------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| **1 确认订单** | 选择道具、数量与金额 | 同左                                                                         | 同左                                                                                                                                                               |
| **2 支付**     | 创建订单后进入支付   | 不跳转；点击「模拟完成支付」→ `POST /api/payment/confirm` 完成订单并发放道具 | 新窗口打开支付宝表单并跳转；用户支付后返回 `return-url`（`/payment/result`）；在弹窗点「我已完成支付」→ `GET /api/payment/order-status` 查询结果，已支付则展示成功 |
| **3 完成**     | 支付成功，道具入背包 | 同左                                                                         | 同左                                                                                                                                                               |

### 接口说明

- **`POST /api/payment/create`**  
  创建订单。请求体：`{ itemId, quantity }`。返回 `orderNo`、`amount`、`payForm`、**`mock`**。
  - `mock === true`：模拟模式，`payForm` 为空，前端走「模拟完成支付」流程。
  - `mock === false`：真实模式，`payForm` 为支付宝表单 HTML，前端新窗口写入并提交，跳转支付宝。

- **`POST /api/payment/confirm`**（仅模拟）  
  按订单号确认支付。请求体：`{ orderNo }`。将订单置为已支付并发放道具。

- **`GET /api/payment/order-status?orderNo=xxx`**（真实模式轮询）  
  查询订单支付状态。返回 `{ orderNo, status, paidAt?, itemName?, quantity }`。`status === 1` 表示已支付。

- **`POST /api/payment/alipay/notify`**（真实模式回调）  
  支付宝异步通知。验证签名后更新订单、发放道具；前端可通过 `order-status` 轮询得到已支付。

### 提示与校验

- 创建订单失败：弹窗内红色提示，可重试。
- 支付中未创建订单即点「模拟/我已完成支付」：提示「请先创建订单」。
- 确认或查询失败：弹窗内红色提示，可重试或「取消支付」。
- 支付成功后关闭弹窗会刷新「我的背包」，道具可在游戏内使用。
- 真实模式从支付宝 return 到 `/payment/result?out_trade_no=xxx` 时，结果页轮询 `order-status`，已支付则展示成功。

---

## 支付宝一键配置（真实环境）

**模拟模式**（默认）：无需任何配置，不跳转支付宝、不扣款，即可完整走通购买流程。

**真实环境**：按下面配置后，将跳转支付宝收银台，支付成功由异步通知 + 前端轮询 `order-status` 完成。

### 1. 环境变量（推荐）

复制 `env.example` 为 `.env`，按需修改：

```bash
cp env.example .env
```

`.env` 示例：

```env
# ===== 安全配置（生产环境必须设置）=====
# JWT 密钥：生产环境必须设置（建议 openssl rand -base64 32）
JWT_SECRET=your-production-jwt-secret-key

# CORS 允许的来源域名（生产环境应限制为实际域名）
CORS_ALLOWED_ORIGINS=https://example.com,https://admin.example.com

# ===== 支付宝配置 =====
# 关闭模拟，启用真实支付宝
ALIPAY_MOCK=false
ALIPAY_APP_ID=你的沙箱/正式AppID
ALIPAY_PRIVATE_KEY=你的应用私钥
ALIPAY_PUBLIC_KEY=支付宝公钥
ALIPAY_NOTIFY_URL=http://localhost:8080/api/payment/alipay/notify
ALIPAY_RETURN_URL=http://localhost:8081/payment/result
```

> **安全提示**：生产环境必须通过环境变量设置 `JWT_SECRET` 和 `CORS_ALLOWED_ORIGINS`，不要使用默认值。

- **Docker Compose**：项目根目录的 `.env` 会被 Compose 用于变量替换；`docker-compose` 已将 `ALIPAY_*` 传给后端服务。
- **本地运行后端**：在 `application.yml` 同目录或通过 `-D` / 系统环境变量配置上述 key，或直接改 `application.yml` 中 `alipay.*`。

### 2. return-url 与 notify-url

- **return-url**：支付完成后浏览器跳转地址，需指向前端支付结果页，如：
  - 本地前端：`http://localhost:5173/payment/result`
  - Docker 用户端：`http://localhost:8081/payment/result`
- **notify-url**：支付宝异步通知地址，需为**后端**可访问的域名 + `/api/payment/alipay/notify`；本地开发可用内网穿透暴露后端。

### 3. 沙箱与正式环境

- 沙箱：`gateway` 使用 `https://openapi-sandbox.dl.alipaydev.com/gateway.do`，使用沙箱 AppID、私钥、支付宝公钥。
- 正式：修改 `gateway`、AppID、密钥等为正式环境配置。

---

## 支付工具说明

### 当前实现

本项目实现了**支付宝支付**（沙箱 + 正式环境），作为题目中"支付宝等支付工具"的核心实现：

- **模拟模式**：默认启用，无需配置，可完整演示购买流程
- **沙箱环境**：支持支付宝沙箱测试
- **正式环境**：配置真实密钥后可用于生产

### 扩展其他支付方式

如需扩展微信支付、银联等其他支付方式，建议按以下方案实现：

1. **后端扩展**
   - 在 `PaymentService` 中新增支付渠道处理方法
   - 创建对应的配置类（如 `WechatPayConfig`）
   - 实现统一的支付回调接口

2. **前端扩展**
   - 在支付弹窗中增加支付方式选择
   - 根据支付方式调用不同的支付接口

3. **配置扩展**
   - 在 `env.example` 中添加对应支付渠道的配置项
   - 支持通过环境变量动态切换支付渠道

> 当前架构已预留扩展空间，新增支付方式无需修改核心业务逻辑。

---

## 技术栈

- **Frontend**: Vue 3 + Vite + Element Plus + Pinia + Axios + Vitest
- **Backend**: Java 17 + Spring Boot 3 + MyBatis-Plus + MySQL 8.0 + JUnit 5 + SpringDoc OpenAPI
- **Payment**: 支付宝沙箱/正式（默认模拟，可一键配置真实环境）
- **Deploy**: Docker + Docker Compose

## H5 适配方案

本项目采用 **postcss-pxtorem + amfe-flexible** 实现移动端响应式适配：

- **设计稿基准**：375px 宽度，1rem = 37.5px
- **自动转换**：px 单位自动转换为 rem，适配不同屏幕
- **桌面端锁定**：768px 以上屏幕锁定 rem 基准，避免过度放大
- **组件库保护**：Element Plus 等组件库样式不转换，保持原有效果

```
// postcss-pxtorem 配置说明
rootValue: 37.5          // 设计稿 375px 时的换算基准
propList: ['*']          // 所有属性都转换
selectorBlackList: ['el-'] // 组件库选择器不转换
```

## 移动端 UI/UX 特性

### 智能导航系统

根据页面场景自动切换导航样式：

| 页面 | 导航样式 | 说明 |
|------|---------|------|
| 游戏页 `/game` | 侧边浮动按钮 | 可拖拽定位，避免遮挡游戏画面 |
| 其他页面 | 底部导航栏 | 传统 Tab 导航，显示图标和文字 |

### 可拖拽浮动按钮

游戏页面的导航和背包按钮支持自由拖拽：

- **导航按钮**（左侧）：点击展开竖向导航菜单
- **背包按钮**（右侧）：点击展开道具列表，使用道具
- **位置记忆**：拖拽位置自动保存到 localStorage，下次访问恢复

### 游戏交互优化

- **背包暂停**：打开背包时游戏自动暂停，关闭后自动恢复
- **面板收起**：点击"继续游戏"或"重新开始"时，自动收起导航和背包
- **全屏布局**：游戏画布最大化，HUD 独立显示在顶部
- **紧凑控制**：方向键布局紧凑，不遮挡游戏内容

### 触控优化

- 移除移动端点击蓝色高亮（`-webkit-tap-highlight-color: transparent`）
- 禁用文本选择避免误触（`user-select: none`）
- 支持触摸拖拽和点击事件

## API 文档

项目集成了 **SpringDoc OpenAPI**，提供自动化 API 文档：

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs

API 文档支持：
- 在线接口测试
- JWT Token 认证
- 请求/响应示例
- 参数校验说明

## 项目结构

```
pacman-game/
├── backend/                 # Spring Boot 后端
│   ├── src/main/java/com/pacman/
│   │   ├── common/         # 通用类（Result、异常等）
│   │   ├── config/         # 配置类（JWT、CORS、OpenAPI等）
│   │   ├── controller/     # 控制器（含 admin 子目录）
│   │   ├── entity/         # 实体类
│   │   ├── mapper/         # MyBatis Mapper
│   │   └── service/        # 服务层
│   ├── src/test/           # 单元测试（Controller、Service）
│   └── Dockerfile
├── frontend-user/           # 用户端前端
│   ├── src/
│   │   ├── api/            # API 接口
│   │   ├── components/     # 可复用组件
│   │   │   ├── game/       # 游戏相关组件（HUD、控制、弹窗等）
│   │   │   └── shop/       # 商城相关组件（道具卡片、购买弹窗等）
│   │   ├── game/           # 游戏引擎模块（Engine、Map、Ghost等）
│   │   ├── router/         # 路由
│   │   ├── stores/         # Pinia 状态
│   │   ├── styles/         # 全局样式
│   │   └── views/          # 页面组件
│   ├── postcss.config.cjs  # PostCSS 配置（pxtorem）
│   ├── vitest.config.js    # 测试配置
│   └── Dockerfile
├── frontend-admin/          # 管理后台前端
│   └── ...
├── scripts/                 # 便捷脚本（dev-start.sh 等）
├── docker-compose.yml       # Docker Compose 配置
├── env.example              # 环境变量示例
└── README.md
```

## 测试

### 后端测试

```bash
cd backend
mvn test
```

### 前端测试

```bash
cd frontend-user
npm install
npm test
```

## License

MIT
