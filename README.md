<div align="center">

# 🌿 区块链食品溯源平台 (Blockchain Food Traceability System)

> 一个基于 FISCO-BCOS 联盟链、Vue 3 与 Spring Boot 构建的去中心化全链路食品溯源系统

[![Vue3](https://img.shields.io/badge/Vue-3.x-4FC08D.svg?logo=vue.js)](https://vuejs.org/)
[![Vite](https://img.shields.io/badge/Vite-5.x-646CFF.svg?logo=vite)](https://vitejs.dev/)
[![SpringBoot](https://img.shields.io/badge/SpringBoot-2.7.x-6DB33F.svg?logo=spring)](https://spring.io/)
[![Java](https://img.shields.io/badge/Java-8-ED8B00.svg?logo=java)](https://www.oracle.com/java/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1.svg?logo=mysql)](https://www.mysql.com/)
[![FISCO-BCOS](https://img.shields.io/badge/FISCO--BCOS-2.x-blue.svg)](https://www.fisco.com.cn/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

</div>

---

## 📖 项目简介

本项目致力于解决传统食品供应链中数据易篡改、信息孤岛及信任度低等痛点。系统底层依托国产开源 **FISCO-BCOS 联盟链**，结合 **Solidity 智能合约**实现核心业务状态的严格流转与角色权限校验。

架构上采用前后端分离模式：前端使用 **Vue 3** 构建，提供数据可视化大屏与消费者端响应式适配；后端基于 **Spring Boot** 搭建高可用 RESTful API 服务。系统采用“链上存证 + 链下查询”的双写机制（交易上链获取 TxHash，数据落库 MySQL 供快速查询），兼顾防篡改与高性能检索。生产环境使用 **Nginx** 托管前端静态资源并反向代理后端接口，保障内网安全。

## ✨ 核心功能与亮点

- 🛡️ **真实链上存证**：所有流转数据均通过 WeBASE-Front 网关上链，前端展示真实的交易哈希（TxHash），实现彻底防伪。
- 🔄 **状态机严格流转**：智能合约利用 `require` 强校验食品生命周期“已生产(0) -> 运输中(1) -> 已送达(2) -> 销售中(3)”，杜绝越权与逻辑跳跃。
- 👥 **四方协同流转**：基于 WeBASE 链上地址鉴权，严格区分供应商、中间商、销售商与消费者。
- 📊 **数据可视化驾驶舱**：基于 ECharts 构建实时监控大屏，动态渲染角色活跃度、食品状态饼图及实时上链动态。
- 🔒 **企业级系统审计**：后端基于 AOP 切面与自定义拦截器，实现全量 API 请求日志入库与 Token 身份鉴权。

---

## 🛠️ 技术栈全览

### 🎨 前端技术栈 (Vue Ecosystem)
| 技术/组件                   | 说明                                                       |
| :-------------------------- | :--------------------------------------------------------- |
| **Vue 3 (Composition API)** | 核心渐进式框架，全量使用 `<script setup>` 语法，逻辑高内聚 |
| **Vite**                    | 极速构建工具，提供现代化的本地开发服务器与打包能力         |
| **Element Plus**            | 基于 Vue 3 的高品质 UI 组件库，构建企业级后台界面          |
| **Vue Router 4**            | 官方路由管理器，配置路由守卫实现前端 Token 鉴权            |
| **Axios**                   | 基于 Promise 的 HTTP 客户端，统一封装拦截器处理请求/响应   |
| **ECharts 5**               | 强大的数据可视化图表库，渲染动态数据大屏                   |
| **CSS3 / 媒体查询**         | 实现炫酷的 UI 动效与全平台移动端响应式布局                 |

### ⚙️ 后端技术栈 (Java Ecosystem)
| 技术/组件              | 说明                                               |
| :--------------------- | :------------------------------------------------- |
| **Java 8**             | 系统底层运行环境，确保极高的兼容性与稳定性         |
| **Spring Boot 2.7.x**  | 核心应用框架，提供自动装配与内嵌 Tomcat 服务器     |
| **Spring MVC**         | 构建标准的 RESTful API 控制层                      |
| **MyBatis**            | 优秀的持久层 ORM 框架，支持 XML 映射与动态 SQL     |
| **MySQL 8.0**          | 关系型数据库，存储链下业务缓存数据与系统日志       |
| **HikariCP**           | 高性能 JDBC 连接池（Spring Boot 默认集成）         |
| **Spring AOP**         | 面向切面编程，拦截 Controller 记录全量操作审计日志 |
| **HandlerInterceptor** | 拦截器，实现基于 Token 的无状态登录鉴权            |
| **RestTemplate**       | Spring HTTP 客户端，用于与 WeBASE-Front 网关通信   |
| **Lombok**             | 注解库，消除实体类的冗余 Getter/Setter 代码        |

### ⛓️ 区块链与基础设施
| 技术/组件            | 说明                                                     |
| :------------------- | :------------------------------------------------------- |
| **FISCO-BCOS 2.9.x** | 国产金融级联盟链底层，采用 PBFT 共识机制                 |
| **Solidity**         | 编写智能合约的核心语言（>=0.6.0），包含严格的状态机控制  |
| **WeBASE-Front**     | 可视化区块链管理平台，提供交易签名、节点监控与 API 代理  |
| **Nginx**            | 高性能反向代理服务器，托管前端静态资源并转发 `/api` 路由 |
| **Ubuntu 22.04**     | 生产环境服务器操作系统（腾讯云轻量应用服务器）           |

---

## 📂 项目目录结构

```text
food-trace-system
├── backend                    # 后端 Spring Boot 项目
│   ├── src/main/java/com/trace
│   │   ├── aspect             # AOP 切面（审计日志）
│   │   ├── config             # Swagger / WebMvc 等配置类
│   │   ├── controller         # 控制层
│   │   ├── entity             # 数据库实体类
│   │   ├── interceptor        # 登录鉴权拦截器
│   │   ├── mapper             # MyBatis 数据访问层
│   │   ├── service            # 业务逻辑层
│   │   └── util               # WeBASE 调用工具类
│   └── src/main/resources
│       ├── mapper             # MyBatis XML 映射文件
│       └── application.yml    # 全局配置文件
│
├── frontend                     # 前端目录
│   └── food-trace-web           # Vue 3 项目主目录 (含 package.json, src)
│       ├── public               # 静态资源（图片必须放在 public/images 下）
│       └── src
│           ├── assets           # 公共样式、字体等
│           ├── components       # 复用组件
│           ├── router           # 路由配置
│           └── views            # 页面视图
│
├── contract                     # 区块链智能合约
│   ├── DataModel.sol            # 数据结构库
│   └── FoodTraceability.sol     # 核心业务合约
│
├── doc                          # 项目文档
│   └── food_trace_db.sql        # 数据库建表脚本
│
├── .gitignore                   # Git 忽略配置
└── README.md