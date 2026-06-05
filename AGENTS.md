# ScriptForge — 项目背景与上下文

给 AI 协作时的完整上下文，以下信息必须在每次对话开始时提供给 AI。

---

## 项目定位

ScriptForge 是一个 AI 小说转剧本工具。将 3 章以上的小说文本自动转换为结构化 YAML 剧本，让作者快速获得可编辑的剧本初稿。

## 时间

72 小时比赛（6月5日 00:00 – 6月7日 23:59）。今天是 Day 1（6月5日）。

## 开发者背景

Java 后端为主，全栈能力有，但实时音视频处理经验不足。

## 技术栈

| 层 | 技术 |
|----|------|
| 后端框架 | Spring Boot 3 + Java 17 |
| ORM | MyBatis + MyBatis-Plus + sqlite-jdbc |
| 数据库 | SQLite（单文件，零配置） |
| LLM 调用 | Spring RestClient 调 OpenAI 兼容 API |
| LLM 提供商 | DeepSeek Chat（默认），可换 Qwen-Plus |
| YAML 处理 | Jackson YAML（jackson-dataformat-yaml） |
| 前端 | Vue 3 + Vite + TypeScript |
| 前端编辑器 | Monaco Editor |
| 部署 | Docker Compose（后端 + 前端 + Nginx） |

## 设计模式

- **策略模式**：ChapterSplitter（多种分章策略）、ScriptConverter（多种 LLM 引擎）
- **管道模式**：上传 → 分章 → LLM 转换 → 校验 → 合并 → 润色 → 输出
- **适配器模式**：中间 YAML Schema 统一表示，各阶段独立

## SQLite 选择原因

- 零配置单文件数据库，评委不需要装 MySQL/PostgreSQL
- 部署简单，不需要 docker-compose 里多一个数据库容器
- 测试用 :memory: 模式，每个测试独立隔离
- 比赛是单用户场景，并发写入不是问题

## LLM 调用架构

- **两阶段转换**：第一阶段生成结构化剧本 → 第二阶段"去 AI 味"润色对话
- System Prompt 包含：角色设定 + Schema 约束 + 风格适配 + Few-shot 示例
- 解析策略：正则提取 ```yaml 代码块 → Jackson YAML parse → 校验 → 失败 retry
- **三级降级**：正常模式 / 缺 Key 引导提示 / API 超时降级

## YAML Schema 设计理念

- 四大顶层分块：metadata / characters / locations / episodes
- 全局角色表 + ID 引用：同一角色跨章节统一
- 全局场景表 + ID 引用：地点归一化
- 多态 content 元素：action / dialogue / transition 按时间线排列
- scene_id 用 "集-场" 格式（如 "1-3"）

## 关键约束

- 每个 PR 只做一件事，粒度尽可能细
- PR 描述必须四段式：标题 + 功能描述 + 实现思路 + 测试方式
- PR 合并后 main 必须可运行
- 第一天结束时必须有可演示的东西
- 第三天只修 bug 和文档，不新增功能
- 命名避免通用教程名（用 SfResult 而非 ApiResult）
- 前端脚手阶段不写 CSS 变量/设计系统

## 18 个 PR 计划

详见 project-brief.md 第三部分"详细实施步骤"。

## 降级原则

- API Key 未配置 → 前端显示引导提示，不白屏
- 提供"体验示例"按钮，加载硬编码 demo 剧本
- 第三方服务超时 → 返回降级结果，不抛异常
