# SmogWatch

面向移动端的雾霾与天气观测应用：自动或手动定位城市，展示实时天气与空气质量（AQI、PM2.5 等），并支持历史查询与区域级联选择。后端聚合百度地图 IP 定位与天气能力，数据落库便于追溯。

**在线仓库**：[github.com/originalsage/smogwatch](https://github.com/originalsage/smogwatch)

## 功能概览

- **定位**：`POST /api/location/resolve` 根据客户端 IP 解析城市；`POST /api/location/manual` 手动提交城市信息。
- **天气与空气质量**：`GET /api/weather/current` 拉取当前天气并写入快照；`GET /api/weather/history` 按城市与日期查询历史；`GET /api/weather/forecast` 逐小时预报（参数为区县编码）。
- **行政区划**：`GET /api/regions/provinces`、`/cities`、`/districts` 供前端级联选城。

## 技术栈

| 层级 | 技术 |
|------|------|
| 前端 | Vue 3、uni-app、Vite |
| 后端 | Spring Boot 3.x、MyBatis、MySQL |
| 外部服务 | 百度地图开放平台（IP 定位、天气） |

## 仓库结构

```
SmogWatch/
├── backend/          # Spring Boot 服务（默认端口 8080）
├── frontend/         # uni-app 前端
├── docs/             # 项目说明与实验文档
└── backend/sql/      # 数据库初始化脚本
```

## 环境要求

- **JDK** 17+
- **Node.js** 18+（建议 LTS，用于前端）
- **MySQL** 8.x（或兼容的 5.7+）
- **百度地图开放平台** [Web 服务 AK](https://lbsyun.baidu.com/)（需开通 IP 定位与天气相关权限）

## 快速开始

### 1. 数据库

在 MySQL 中执行：

```bash
mysql -u root -p < backend/sql/init.sql
```

脚本会创建库 `smogwatch` 及 `weather_snapshot`、`weather_district` 等表。若运行时报错缺少 `city_location_log`，请根据 `docs/项目报告.md` 中的表设计自行建表后再启动。

### 2. 后端配置

编辑 `backend/src/main/resources/application.yml`（推荐复制为 **`application-local.yml`** 并仅在本机使用，避免将密钥提交到 Git）：

- `spring.datasource.url`、`username`、`password`：指向你的 MySQL。
- `baidu.map.ak`：百度地图 AK。
- `baidu.map.location-url`、`weather-url`：一般保持默认即可。
- `app.cors.allowed-origins`：生产环境建议改为具体前端域名，而非 `*`。

启动：

```bash
cd backend
mvn spring-boot:run
```

服务就绪后默认地址：`http://127.0.0.1:8080`。

### 3. 前端配置与运行

在 `frontend` 目录创建或修改环境变量（勿将含密钥的 `.env` 提交到公开仓库）：

- **`VITE_API_BASE`**：后端 API 根地址，本地联调示例：`http://127.0.0.1:8080`。

`src/utils/http.js` 在未设置 `VITE_API_BASE` 时会回退到 `http://127.0.0.1:8080`。

```bash
cd frontend
npm install
npm run dev:h5
```

其他端（微信小程序等）可使用 package.json 中对应的 `dev:mp-*` / `build:mp-*` 脚本。

### 4. 部署与反向代理

若后端部署在网关或 Ingress 之后，需正确传递 **`X-Forwarded-For`** / **`X-Real-IP`** / **`Forwarded`**，以便 IP 定位接口能拿到用户公网 IP。仓库中已配置 `server.forward-headers-strategy: framework`。

## API 一览

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/location/resolve` | IP 解析定位 |
| POST | `/api/location/manual` | 手动城市 |
| GET | `/api/weather/current?city=` | 当前天气（城市名或区县编码） |
| GET | `/api/weather/history?city=&date=YYYY-MM-DD` | 历史快照 |
| GET | `/api/weather/forecast?city=` | 24 小时预报（区县编码） |
| GET | `/api/regions/provinces` | 省份列表 |
| GET | `/api/regions/cities?province=` | 城市列表 |
| GET | `/api/regions/districts?province=&city=&cityGeocode=` | 区县列表 |

统一响应封装为 `ApiResponse`（成功时 `data` 为业务体，失败时含错误信息）。

## 文档

更完整的架构说明、数据库字段与测试记录见 **`docs/项目报告.md`**。

## 许可证

若需开源协议，请在仓库中补充 `LICENSE` 文件并在此更新说明。
