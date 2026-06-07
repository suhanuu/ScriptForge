# ScriptForge 剧本 YAML Schema 设计文档

## 设计目标

这套 Schema 同时服务两个使用者：**人类作者**（可读可编辑）和**机器系统**（可解析可分析）。三个核心目标：

1. **可编辑**：作者拿到 YAML 后能直接用文本编辑器修改台词、增删场景
2. **可渲染**：剧本编辑器/渲染器可根据 Schema 自动生成分镜、对白表、角色出场统计
3. **可版本控制**：纯文本 YAML，天然适合 Git diff，支持多轮修改追踪

---

## Schema 总览

### 四大顶层分块

```
ScriptOutput
├── metadata        # 剧本元信息
├── characters[]     # 全局角色表
├── locations[]      # 全局场景地点表
└── episodes[]       # 剧集列表
      └── scenes[]   # 场景列表
            └── content[]  # 多态内容元素
```

### 完整示例

```yaml
metadata:
  title: "深夜来客"
  original_work: "深夜来客（示例小说）"
  author: "未知"
  version: "1.0"
  created_at: "2026-06-07T12:00:00Z"
  estimated_duration: 45

characters:
  - id: "lin_yuan"
    name: "林远"
    role: "主角"
    description: "28岁程序员，性格内敛但技术过硬"

  - id: "zhou_ming"
    name: "周明"
    role: "配角"
    description: "技术总监，40岁，沉稳但此刻语气异常紧张"

locations:
  - id: "apt_linyuan"
    name: "林远的公寓"
    type: "内景"
    description: "普通的单身公寓，略显杂乱"

  - id: "office_tech"
    name: "公司技术部"
    type: "内景"
    description: "深夜的办公室，只开了半排灯"

episodes:
  - episode_id: 1
    title: "第一集：深夜来客"
    scenes:
      - scene_id: "1-1"
        location: "apt_linyuan"
        time: "深夜"
        weather: "雨"
        characters_present: ["lin_yuan"]
        content:
          - type: "action"
            text: "手机闹钟响了三遍，林远从被窝里伸出一只手摸索手机"
          - type: "dialogue"
            character: "lin_yuan"
            text: "关掉。"
            parenthetical: "哑着嗓子"
          - type: "transition"
            effect: "切入"
            next_scene: "1-2"

      - scene_id: "1-2"
        location: "office_tech"
        time: "凌晨"
        weather: "雨转晴"
        characters_present: ["lin_yuan", "zhou_ming"]
        content:
          - type: "action"
            text: "林远推开办公室玻璃门"
          - type: "dialogue"
            character: "zhou_ming"
            text: "你看这个。"
            parenthetical: "把屏幕转向林远"
          - type: "transition"
            effect: "无"
            next_scene: null
```

---

## 逐字段说明

### metadata — 剧本元信息

| 字段 | 类型 | 必填 | 说明 |
|------|------|:---:|------|
| `title` | string | ✅ | 剧本标题，取原文件名或小说标题 |
| `original_work` | string | ✅ | 原作名称，保留溯源信息 |
| `author` | string | ✅ | 原书作者，"未知"表示信息缺失 |
| `version` | string | | 版本号，支持多轮修改迭代（默认 1.0） |
| `created_at` | string | | ISO 8601 时间戳，记录生成时间 |
| `estimated_duration` | int | | 预估时长（分钟），按每场景 2 分钟估算 |

### characters — 全局角色表

| 字段 | 类型 | 必填 | 说明 |
|------|------|:---:|------|
| `id` | string | ✅ | 唯一标识，推荐拼音全拼（如 `lin_yuan`） |
| `name` | string | ✅ | 角色名称 |
| `role` | string | ✅ | 角色定位：主角/配角/反派/路人 |
| `description` | string | | 简短人物描述，LLM 从上下文推断补充 |

### locations — 全局场景地点表

| 字段 | 类型 | 必填 | 说明 |
|------|------|:---:|------|
| `id` | string | ✅ | 唯一标识，推荐英文缩写（如 `apt_linyuan`） |
| `name` | string | ✅ | 地点名称 |
| `type` | string | ✅ | 内景/外景 |
| `description` | string | | 场景描述，帮助美术和置景理解氛围 |

### episodes — 剧集列表

| 字段 | 类型 | 必填 | 说明 |
|------|------|:---:|------|
| `episode_id` | int | ✅ | 集序号，从 1 开始 |
| `title` | string | ✅ | 集标题，如"第一集：深夜来客" |
| `scenes` | Scene[] | ✅ | 该集下的场景列表 |

### scenes — 场景定义

| 字段 | 类型 | 必填 | 说明 |
|------|------|:---:|------|
| `scene_id` | string | ✅ | "集-场" 格式编号（如 `1-1`），增删场景不破坏 ID 稳定性 |
| `location` | string | ✅ | 引用 `locations[].id` |
| `time` | string | | 时间段：凌晨/白天/夜晚/深夜 |
| `weather` | string | | 天气：晴/雨/雪/风 |
| `characters_present` | string[] | | 本场出现角色 ID 列表，方便导演排期 |
| `content` | Content[] | ✅ | 按时间线排列的多态内容元素 |

### content — 多态内容元素

content 数组中的每个元素通过 `type` 字段区分类型：

**action（动作元素）**

| 字段 | 类型 | 说明 |
|------|------|------|
| `type` | string | 固定值 `"action"` |
| `text` | string | 动作描述，可用括号标注氛围 |

**dialogue（对白元素）**

| 字段 | 类型 | 说明 |
|------|------|------|
| `type` | string | 固定值 `"dialogue"` |
| `character` | string | 说话人的角色 ID |
| `text` | string | 台词文本 |
| `parenthetical` | string | 括号提示语气（如 "哑着嗓子"） |

**transition（转场元素）**

| 字段 | 类型 | 说明 |
|------|------|------|
| `type` | string | 固定值 `"transition"` |
| `effect` | string | 切入/淡入/黑幕/闪回/无 |
| `next_scene` | string | 下一场 scene_id，本集末场为 null |

---

## 10 项设计决策理由

### 1. 四大顶层分块（metadata / characters / locations / episodes）

关注点分离。元信息、角色、地点是全局复用资源，剧集是核心内容。修改角色描述只改 `characters` 一处，不用每个 scene 里逐个改。导演可以只看 `characters` 表排演员档期。

### 2. 全局角色表 + ID 引用

同一角色跨多个 scene 出现时，通过 `characters[].id` → `content[].character` 引用。机器可按 ID 统计出场次数和台词量，导演一眼知道每个演员的工作量。避免了 `speaker: "张三"` 在不同 scene 中被写成"张三""小张""张总"导致的对不齐问题。

### 3. 全局场景地点表 + ID 引用

归一化地点名称。"废弃仓库""旧仓库""3号仓库"指同一地点的不同写法，在 `locations` 表中统一为一个 ID。支持场景频次统计——哪里的戏最多一目了然。

### 4. 三级嵌套 episodes → scenes → content

支持长篇小说多集拆分。每集独立管理标题和时长，content 按时间线排列保证导演从头读到尾不跳戏。对比扁平 `scenes` 数组，多了一层集的概念更适合长篇改编。

### 5. content 用 `type` 做多态

动作（action）、对白（dialogue）、转场（transition）按时间线交替排列在同一个 content 数组中，而不是分成 `dialogues[]`、`actions[]`、`transitions[]` 三个独立数组。这样导演从头读到尾，不用在三个数组间反复横跳。Java 端用 `@JsonTypeInfo` + `@JsonSubTypes` 注解实现自动反序列化。

### 6. dialogue 带 `character`（角色 ID）和 `parenthetical`（括号提示）

一眼知道谁在说话、什么语气，符合好莱坞标准剧本格式。`parenthetical` 是 screenplay 标准元素——括号内简注，导演讲戏时用来提示演员。

### 7. transition 带 `effect` 和 `next_scene`

转场效果（切入/淡入/黑幕/闪回）是后期剪辑的关键信息。`next_scene` 构成隐式双向链表——剧本渲染器可以据此生成转场时间线，也能检测出死循环或孤立场景。

### 8. metadata 含 version + created_at

YAML 作为 Git 友好的纯文本格式，版本追踪天然优势。`version` 支持多轮修改迭代（1.0 → 1.1 → 2.0），`created_at` 记录生成时间。多轮修改后 diff 能看到每次改了什么。

### 9. estimated_duration 自动计算

每 scene 按 2 分钟估算，`metadata.estimated_duration = count(scenes) × 2`。导演一眼知道改编后的剧本大概多长，也不需要精确——手动编辑后可以改。

### 10. scene_id 用 "集-场" 格式（如 "1-3"）

删除/插入场景后不影响其他 scene 的 ID 稳定性。假设在 1-2 和 1-3 之间插入一场新戏，命名为 1-2.5 或 1-3a 即可，不用把后面所有 ID 往后推一位。

---

## 扩展性预留

当前 Schema 覆盖 P0 核心功能。以下字段按需在后续版本中加入，Schema 设计已预留嵌套结构：

| 扩展方向 | 预留字段 | 说明 |
|---------|---------|------|
| 灯光指示 | `scene.lighting` | 如 "昏暗""明亮""烛光"。灯光师参考 |
| 道具清单 | `scene.props[]` | 每场需要的道具列表。道具组准备 |
| 服装提示 | `scene.costumes[]` | 角色在本场的着装要求 |
| 镜头语言 | `scene.shots[]` | 如 "特写""全景""跟拍"。供分镜师参考 |
| 音效/配乐 | `scene.audio[]` | 背景音乐、环境音效。后期配音参考 |
| 章节备注 | `scene.notes` | 导演备注字段（已支持，值为可选 string） |

---

## 与 Dramatron 的对比

Dramatron（DeepMind, 2022）提出的分层 Prompt-Chaining 将剧本生成拆为 Log Line → 标题 → 角色 → 场景节拍 → 地点 → 对白六个阶段。我们借鉴了其分阶段思想，但做了两个关键简化以适应 72 小时比赛：

1. **不拆六层**：我们采用两阶段（生成 + 润色），每阶段独立校验，减少 LLM 调用次数和 token 消耗
2. **角色表 + 地点表前置**：Dramatron 的场景节拍和角色提取是独立阶段，我们合并到单次 LLM 调用中——让 LLM 先通读全文一次性提取所有角色和地点，再逐场生成 content
