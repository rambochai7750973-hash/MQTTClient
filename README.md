# Android MQTT Client — AI 生成规格书

## 0. 生成指令

使用 Kotlin + Jetpack Compose + MVVM + Hilt + Room + Eclipse Paho。
**严格遵循以下规格逐文件生成代码，不遗漏任何文件。完整项目结构见第 5 节。**

---

## 1. 功能规格

### 1.1 连接管理

| # | 功能 | 详细 |
|---|------|------|
| C1 | 协议支持 | tcp:// (1883) / ssl:// (8883) / ws:// (8083) / wss:// (8084) |
| C2 | 客户端 ID | 自动生成（UUID 前 8 位）或手动输入；为空时 Broker 自动分配 |
| C3 | 认证 | 用户名+密码 / 匿名 / Token（自定义 Header） |
| C4 | TLS | 三种模式：无加密 / 信任所有证书（`TRUST_ALL`）/ 自定义 CA 证书（`assets/` 或文件选择器） |
| C5 | Clean Session | 开关，默认 true；false 时需同时设置会话过期时间（1-86400 秒） |
| C6 | 自动重连 | 开关 + 间隔选择（1/2/5/10/30 秒），默认开启 5 秒 |
| C7 | 超时 | 连接超时（默认 10s）、Keep Alive（默认 60s）均可配置 |
| C8 | Last Will | Topic + Payload + QoS + Retain；启用后才展开子表单 |
| C9 | 状态机 | `DISCONNECTED → CONNECTING → CONNECTED → DISCONNECTING → DISCONNECTED`，异常时 → `ERROR` |
| C10 | 状态 UI | 指示灯（绿/黄/红/灰）+ 文字 + 连接时长；点击展开详情面板 |
| C11 | WebSocket | 额外支持 Path（如 `/mqtt`）和 HTTP Headers 配置 |

### 1.2 订阅管理

| # | 功能 | 详细 |
|---|------|------|
| S1 | 订阅输入 | Topic 输入框 + QoS Radio 组 + 订阅按钮 |
| S2 | 通配符 | 支持 `+`（单层）和 `#`（多层），客户端不做过滤，完全依赖 Broker |
| S3 | 列表展示 | LazyColumn，每项显示 Topic（代码字体）、QoS 徽章、订阅时间；右滑取消 |
| S4 | 去重 | 同一 Topic 重复订阅自动忽略（保留首次 QoS） |
| S5 | 重连恢复 | 自动重连时重新订阅所有 Topic（从 ViewModel 中的 Set 读取） |
| S6 | 批量订阅 | 输入框支持多行粘贴，每行一个 Topic，统一使用同一 QoS |

### 1.3 消息发布

| # | 功能 | 详细 |
|---|------|------|
| P1 | 表单 | Topic 输入（带最近 10 个历史 Topic 自动补全）+ Payload 多行输入 + QoS + Retain |
| P2 | Payload 类型 | 支持文本（UTF-8）和 Hex 输入两种模式，Tab 切换 |
| P3 | 发送确认 | QoS=0 即刻完成；QoS=1 等待 `deliveryComplete` 回调后标记 ✓；QoS=2 同样 |
| P4 | 发送失败 | 消息标记 `isDelivered=false`，列表显示红色 ⚠，可点击重发 |
| P5 | Topic 持久化 | 发布的 Topic 自动存入 Room `publish_history`，下次输入时下拉提示 |

### 1.4 消息列表

| # | 功能 | 详细 |
|---|------|------|
| M1 | 列表布局 | LazyColumn，最新在底部，自动滚动（可开关），10 条/页分页 |
| M2 | 消息卡片 | 显示 Topic 标签（彩色 chip）、Payload 预览（截断 120 字符）、QoS 角标、📌 Retain 标记、时间戳 |
| M3 | 搜索过滤 | 顶部搜索框，按 Topic 实时过滤（客户端过滤已加载数据） |
| M4 | Payload 交互 | 点击 → BottomSheet 展示全文 + 格式化；长按 → 弹出菜单：复制 Payload / 复制 Topic / 分享 / Hex 视图 |
| M5 | 自动格式化 | 以 `{`/`[` 开头 → JSON 格式化；全 Hex 字符 → Hex 视图按钮；纯数字 → 十进制/十六进制/二进制切换 |
| M6 | 清空 | 清空当前列表（内存），不影响 Room 历史 |

### 1.5 多连接管理

| # | 功能 | 详细 |
|---|------|------|
| N1 | 配置 CRUD | 新建 / 编辑 / 复制 / 删除，全部持久化到 Room |
| N2 | 快速连接 | 列表项点击直接切换连接（先断开当前，再连接目标） |
| N3 | 导入导出 | 全部配置序列化为 JSON 文件（`config_backup.json`），支持导入恢复 |
| N4 | 表单校验 | 保存时校验：名称非空、地址非空、端口 1-65535、QoS 0-2 |

### 1.6 消息历史与日志

| # | 功能 | 详细 |
|---|------|------|
| H1 | 入库 | 每条收发消息写入 Room `message_history`，字段：topic, payload(BLOB), qos, retained, is_incoming, is_delivered, timestamp |
| H2 | 查询 | 按 Topic 模糊搜索 + 时间范围（全部/今天/最近7天/自定义） |
| H3 | 限额 | 默认保留 1000 条，超出自动删除最旧 100 条 |
| H4 | 日志 | ViewModel 持有 `MutableStateFlow<List<LogEntry>>`，日志等级 DEBUG/INFO/WARN/ERROR；UI 中日志页面可滚动查看 |

---

## 2. 技术架构

### 2.1 架构总览

```
┌─────────────────────────────────────────────────────────────┐
│                      UI Layer (Compose)                      │
│  MessagesScreen  SubscriptionsScreen  PublishScreen  ...    │
│         ▲ StateFlow<UiState>          │ event (lambda)       │
├─────────┼─────────────────────────────┼─────────────────────┤
│         │        ViewModel            │                     │
│  MessagesVM  SubscriptionsVM  PublishVM  ConnectionVM ...   │
│         ▲ Flow / suspend              │ fun call             │
├─────────┼─────────────────────────────┼─────────────────────┤
│         │        Repository           │                     │
│         │  MqttRepository  ConnectionRepository              │
│         │  MessageRepository          │                     │
├─────────┼─────────────────────────────┼─────────────────────┤
│         │        Data Source          │                     │
│  ┌──────┴──────┐           ┌─────────┴────────┐            │
│  │  Room DB    │           │  MqttManager     │            │
│  │  (DAOs)     │           │  (Paho wrapper)  │            │
│  └─────────────┘           │  MqttService     │            │
│                            └──────────────────┘            │
└─────────────────────────────────────────────────────────────┘
```

### 2.2 核心类契约

```kotlin
// ============ MqttManager.kt ============
@Singleton
class MqttManager @Inject constructor(
    private val mqttClientFactory: MqttClientFactory
) {
    val connectionState: StateFlow<ConnectionState>
    val incomingMessages: SharedFlow<MqttMessage>
    val deliveryComplete: SharedFlow<Int>  // messageId

    suspend fun connect(config: ConnectionConfig): Result<Unit>
    suspend fun disconnect(): Result<Unit>
    suspend fun subscribe(topic: String, qos: Int): Result<Unit>
    suspend fun unsubscribe(topic: String): Result<Unit>
    suspend fun publish(topic: String, payload: ByteArray, qos: Int, retain: Boolean): Result<Int>  // returns messageId
}

// ============ MqttRepository.kt ============
@Singleton
class MqttRepository @Inject constructor(
    private val mqttManager: MqttManager,
    private val messageDao: MessageHistoryDao,
    private val publishDao: PublishHistoryDao
) {
    // 转发 MqttManager 的 StateFlow，同时将消息写入 Room
    val messages: Flow<List<MqttMessage>>  // 合并实时消息 + 历史消息
    val connectionState: StateFlow<ConnectionState>

    suspend fun saveMessage(msg: MqttMessage)
    suspend fun searchMessages(query: String, timeRange: TimeRange): List<MqttMessage>
    suspend fun deleteOldMessages(limit: Int = 1000)
}

// ============ ConnectionState.kt ============
sealed interface ConnectionState {
    data object Disconnected : ConnectionState
    data object Connecting : ConnectionState
    data class Connected(val serverUri: String, val clientId: String, val connectedSince: Long) : ConnectionState
    data class Disconnecting(val reason: String? = null) : ConnectionState
    data class Error(val reason: String, val throwable: Throwable? = null) : ConnectionState
}

// ============ MqttMessage.kt ============
data class MqttMessage(
    val id: Long = 0,
    val topic: String,
    val payload: ByteArray,
    val qos: Int,
    val retained: Boolean,
    val isIncoming: Boolean,
    val timestamp: Long,
    val isDelivered: Boolean? = null  // null for incoming, true/false for outgoing
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MqttMessage) return false
        return id == other.id && topic == other.topic &&
               payload.contentEquals(other.payload) && qos == other.qos &&
               retained == other.retained && isIncoming == other.isIncoming &&
               timestamp == other.timestamp
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + topic.hashCode()
        result = 31 * result + payload.contentHashCode()
        result = 31 * result + qos
        return result
    }
}
```

### 2.3 ViewModel 模板

每个 ViewModel 遵循以下模式：

```kotlin
@HiltViewModel
class XxxViewModel @Inject constructor(
    private val repository: MqttRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(XxxUiState())
    val uiState: StateFlow<XxxUiState> = _uiState.asStateFlow()

    init {
        // 收集 repository 的 Flow
        viewModelScope.launch {
            repository.messages.collect { messages ->
                _uiState.update { it.copy(messages = messages) }
            }
        }
    }

    fun onEvent(event: XxxEvent) {
        when (event) {
            // ...
        }
    }
}

data class XxxUiState(
    val isLoading: Boolean = false,
    val messages: List<MqttMessage> = emptyList(),
    val error: String? = null
)

sealed interface XxxEvent {
    // ...
}
```

### 2.4 错误处理

| 场景 | ConnectionState | UI 反馈 |
|------|----------------|---------|
| 连接超时 | `Error("连接超时")` | Snackbar: "连接超时，请检查地址和网络" |
| 认证失败 | `Error("认证失败")` | Snackbar: "用户名或密码错误" |
| Broker 不可达 | `Error("无法连接")` | Snackbar: "无法连接到 {host}:{port}" |
| 网络断开 | `Disconnected` → 自动重连 → `Connecting` | 状态条显示 "网络断开，{n} 秒后重连..." |
| TLS 证书错误 | `Error("SSL 错误")` | Dialog: "证书验证失败" + 详情 |
| 订阅失败 | 不改变连接状态，日志 ERROR | Snackbar: "订阅 {topic} 失败" |
| 发布失败 | 不改变连接状态，`isDelivered=false` | 列表该条消息显示 ⚠，可点击重发 |

---

## 3. UI 布局规格

### 3.1 导航

```
BottomNavigationBar (4 tabs):
  Tab 1: 消息     icon=Icons.Default.Email        → MessagesScreen     (default)
  Tab 2: 订阅     icon=Icons.Default.Subscriptions → SubscriptionsScreen
  Tab 3: 发布     icon=Icons.Default.Send          → PublishScreen
  Tab 4: 设置     icon=Icons.Default.Settings      → SettingsScreen
      └→ 连接配置   → SavedConfigsScreen
      └→ 应用设置   → AppSettingsScreen
      └→ 日志       → LogScreen
      └→ 关于       → AboutScreen
```

### 3.2 消息页（默认首页）布局

```
┌──────────────────────────────────────────┐
│ [● 已连接] broker.emqx.io:1883    [断开]  │ ← ConnectionStatusBar
│                          已连接 12m 34s    │   (点击展开详情)
├──────────────────────────────────────────┤
│    [消息]     订阅     发布               │ ← TabRow
├──────────────────────────────────────────┤
│  🔍 搜索 Topic...                         │ ← search query
├──────────────────────────────────────────┤
│ ┌─ sensor/temp ─── QoS 1 ─ 14:30:25 ─┐  │
│ │ {"temperature":25.6,"unit":"c"}     │  │ ← MessageCard
│ │ 📌 Retained                         │  │
│ └─────────────────────────────────────┘  │
│ ┌─ sensor/humidity ─ QoS 0 ─ 14:30:20 ┐  │
│ │ 78.3                                │  │
│ └─────────────────────────────────────┘  │
│ ...                                      │
│                                [⬇ 最新]  │ ← scrollToBottom FAB
├──────────────────────────────────────────┤
│  消息 0/0                 [清空] [导出]   │
└──────────────────────────────────────────┘
```

### 3.3 订阅页布局

```
┌──────────────────────────────────────────┐
│  Topic               QoS    [订阅]        │ ← 顶部操作栏
│ [__________________] [0 ●1 ○2 ○] [订阅]  │
├──────────────────────────────────────────┤
│  已订阅 (3)                              │
│ ┌─ sensor/# ──────────── QoS 1 ─── [✕] ┐│
│ │  订阅于 14:25:10                      ││ ← 左滑出现红色删除背景
│ └───────────────────────────────────────┘│
│ ┌─ actuator/led ──────── QoS 2 ─── [✕] ┐│
│ └───────────────────────────────────────┘│
│ ┌─ $SYS/broker/+/# ──── QoS 0 ─── [✕] ┐│
│ └───────────────────────────────────────┘│
└──────────────────────────────────────────┘
```

### 3.4 发布页布局

```
┌──────────────────────────────────────────┐
│  发布消息                                │
├──────────────────────────────────────────┤
│  Topic                                   │
│  [sensor/temp___________________] [▼]    │ ← 下拉历史
├──────────────────────────────────────────┤
│  Payload                                 │
│  ┌────────────────────────────────────┐  │
│  │ {"temperature": 25.6}             │  │ ← 多行输入
│  │                                    │  │
│  └────────────────────────────────────┘  │
│  [文本] [Hex]                             │ ← Payload 模式切换
├──────────────────────────────────────────┤
│  QoS  ○0  ●1  ○2     Retain [□]         │
├──────────────────────────────────────────┤
│              [  📤  发送  ]              │ ← 全宽按钮
└──────────────────────────────────────────┘
```

### 3.5 连接设置页布局

```
┌──────────────────────────────────────────┐
│  [←] 连接配置                             │
├──────────────────────────────────────────┤
│  名称     [我的 EMQX 服务器      ]       │
│  协议     [tcp:// ● ssl:// ○ ws:// ○]   │
│  主机     [broker.emqx.io         ]      │
│  端口     [8883                   ]      │
│  客户端ID [mqtt_client_a1b2      ] [🔄]  │
│  用户名   [admin                  ]      │
│  密码     [••••••••               ]      │
├──────────── 高级设置 ────────────────────┤
│  Clean Session    [✓]                    │
│  自动重连         [✓]  间隔 [● 5s ▼]    │
│  连接超时         [10 秒         ]       │
│  Keep Alive       [60 秒         ]       │
│  会话过期时间     [0 秒(不过期)  ]       │
├──────────── Last Will ───────────────────┤
│  启用             [□]                    │
│  Topic    [device/status         ]       │
│  Payload  [offline               ]       │
│  QoS      [○0 ●1 ○2]  Retain [✓]        │
├──────────── TLS/SSL ─────────────────────┤
│  启用             [✓]                    │
│  验证模式  [● 信任所有  ○ 自定义CA]     │
│  CA 证书   [选择文件...]  ca.crt         │
│  客户端证书 [选择文件...]  client.crt    │
│  客户端密钥 [选择文件...]  client.key    │
├──────────────────────────────────────────┤
│  [测试连接]    [保存]    [连接]           │
└──────────────────────────────────────────┘
```

### 3.6 空状态 / 加载 / 错误

| 场景 | 显示 |
|------|------|
| 消息列表为空（未连接） | 插画 + "尚未连接，请先连接 MQTT Broker" + [去连接] 按钮 |
| 消息列表为空（已连接） | 插画 + "等待消息中..." + 动画 loading |
| 搜索无结果 | "未找到匹配的消息" |
| 订阅列表为空 | 插画 + "尚未订阅任何 Topic" |
| 网络错误 | 插画 + "网络异常" + [重试] 按钮 |
| 加载中 | 居中 CircularProgressIndicator |

---

## 4. 数据层

### 4.1 Room Entity

```kotlin
@Entity(tableName = "connection_config")
data class ConnectionConfigEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,                      // 配置模板名称
    val protocol: String = "tcp",          // tcp | ssl | ws | wss
    val host: String,                      // Broker 地址
    val port: Int = 1883,                  // 端口
    val clientId: String?,                 // null = AutoGenerate
    val username: String?,
    val password: String?,
    val cleanSession: Boolean = true,
    val autoReconnect: Boolean = true,
    val reconnectIntervalSec: Int = 5,
    val connectTimeoutSec: Int = 10,
    val keepAliveSec: Int = 60,
    val sessionExpirySec: Int = 0,
    val willTopic: String?,
    val willPayload: String?,
    val willQos: Int = 0,
    val willRetain: Boolean = false,
    val tlsEnabled: Boolean = false,
    val tlsTrustAll: Boolean = true,
    val wsPath: String? = null,            // WebSocket path, e.g. /mqtt
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "message_history",
    indices = [
        Index("topic"),
        Index("timestamp")
    ]
)
data class MessageHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val topic: String,
    val payload: ByteArray,
    val qos: Int,
    val retained: Boolean = false,
    val isIncoming: Boolean = true,
    val isDelivered: Boolean? = null,
    val timestamp: Long
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MessageHistoryEntity) return false
        return id == other.id && topic == other.topic &&
               payload.contentEquals(other.payload) && qos == other.qos &&
               retained == other.retained && isIncoming == other.isIncoming &&
               timestamp == other.timestamp
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + topic.hashCode()
        result = 31 * result + payload.contentHashCode()
        result = 31 * result + qos
        return result
    }
}

@Entity(tableName = "publish_history")
data class PublishHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val topic: String,
    val lastUsedAt: Long
)
```

### 4.2 DAO

```kotlin
@Dao
interface MessageHistoryDao {
    @Query("SELECT * FROM message_history ORDER BY timestamp DESC LIMIT :limit OFFSET :offset")
    fun getMessagesPaged(limit: Int, offset: Int): Flow<List<MessageHistoryEntity>>

    @Query("SELECT * FROM message_history WHERE topic LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchMessages(query: String): Flow<List<MessageHistoryEntity>>

    @Query("SELECT * FROM message_history WHERE timestamp BETWEEN :start AND :end ORDER BY timestamp DESC")
    fun getMessagesByTimeRange(start: Long, end: Long): Flow<List<MessageHistoryEntity>>

    @Insert
    suspend fun insert(message: MessageHistoryEntity): Long

    @Query("DELETE FROM message_history WHERE id IN (SELECT id FROM message_history ORDER BY timestamp ASC LIMIT :count)")
    suspend fun deleteOldest(count: Int)

    @Query("DELETE FROM message_history")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM message_history")
    suspend fun count(): Int
}
```

---

## 5. 完整项目文件清单

生成以下每个文件，一个不少：

```
com.example.mqttclient/
├── App.kt                                                  # @HiltAndroidApp
├── MainActivity.kt                                         # setContent { NavGraph() }
│
├── di/
│   ├── AppModule.kt                                        # 提供 Context, SharedPreferences
│   ├── DatabaseModule.kt                                   # 提供 AppDatabase + DAOs
│   └── MqttModule.kt                                       # 提供 MqttManager (单例)
│
├── data/
│   ├── local/
│   │   ├── AppDatabase.kt                                  # Room DB, version 1
│   │   ├── dao/
│   │   │   ├── ConnectionConfigDao.kt
│   │   │   ├── MessageHistoryDao.kt
│   │   │   └── PublishHistoryDao.kt
│   │   └── entity/
│   │       ├── ConnectionConfigEntity.kt
│   │       ├── MessageHistoryEntity.kt
│   │       └── PublishHistoryEntity.kt
│   ├── repository/
│   │   ├── MqttRepository.kt
│   │   ├── ConnectionRepository.kt
│   │   └── MessageRepository.kt
│   └── model/
│       ├── ConnectionState.kt
│       ├── MqttMessage.kt
│       └── ConnectionConfig.kt                              // Domain model (与 Entity 分离)
│
├── mqtt/
│   ├── MqttManager.kt
│   ├── MqttClientFactory.kt                                // 创建 MqttAndroidClient，方便 Mock
│   └── MqttCallbackHandler.kt                              // 统一回调 → StateFlow
│
├── ui/
│   ├── navigation/
│   │   ├── Screen.kt                                       // sealed class 路由定义
│   │   └── NavGraph.kt                                     // NavHost + composable()
│   ├── theme/
│   │   ├── Theme.kt                                        // MaterialTheme 包装
│   │   ├── Color.kt                                        // 亮/暗色板
│   │   └── Type.kt                                         // Typography
│   ├── components/
│   │   ├── ConnectionStatusBar.kt                          // 顶部连接状态条
│   │   ├── MessageCard.kt                                  // 单条消息卡片
│   │   ├── QosSelector.kt                                  // QoS RadioButton 组
│   │   ├── TopicAutoCompleteField.kt                       // 带自动补全的 Topic 输入
│   │   ├── EmptyStateView.kt                               // 空状态插画组件
│   │   ├── PayloadBottomSheet.kt                           // Payload 详情的 BottomSheet
│   │   └── ConfirmDialog.kt                                // 确认弹窗
│   ├── screen/
│   │   ├── messages/
│   │   │   ├── MessagesScreen.kt
│   │   │   └── MessagesViewModel.kt
│   │   ├── subscriptions/
│   │   │   ├── SubscriptionsScreen.kt
│   │   │   └── SubscriptionsViewModel.kt
│   │   ├── publish/
│   │   │   ├── PublishScreen.kt
│   │   │   └── PublishViewModel.kt
│   │   ├── connection/
│   │   │   ├── ConnectionSettingsScreen.kt                 // 新建/编辑连接配置
│   │   │   └── ConnectionSettingsViewModel.kt
│   │   ├── configs/
│   │   │   ├── SavedConfigsScreen.kt                       // 已保存配置列表
│   │   │   └── SavedConfigsViewModel.kt
│   │   ├── settings/
│   │   │   ├── SettingsScreen.kt
│   │   │   └── SettingsViewModel.kt
│   │   └── log/
│   │       ├── LogScreen.kt
│   │       └── LogViewModel.kt
│   └── util/
│       ├── ClipboardHelper.kt
│       └── PayloadFormatter.kt                             // JSON/Hex/Base64 格式化
│
├── service/
│   ├── MqttForegroundService.kt                            // 前台 Service
│   └── NotificationHelper.kt                               // 通知渠道 + 构建通知
│
├── util/
│   ├── JsonFormatter.kt
│   └── TimeUtils.kt
│
└── receiver/
    └── NetworkChangeReceiver.kt                            // 监听网络变化触发重连
```

共 **40+ 个文件**，每个文件生成时需包含完整 import、注解、TODO 标记。

---

## 6. 依赖 & 构建

### 6.1 libs.versions.toml

```toml
[versions]
kotlin = "2.0.21"
agp = "8.7.3"
compose-bom = "2024.12.01"
hilt = "2.53.1"
room = "2.6.1"
paho = "1.2.5"
coroutines = "1.9.0"
lifecycle = "2.8.7"
navigation = "2.8.5"
activity-compose = "1.9.3"
ksp = "2.0.21-1.0.27"
junit = "5.11.4"
mockk = "1.13.14"
turbine = "1.2.0"

[libraries]
paho-android-service = { module = "org.eclipse.paho:org.eclipse.paho.android.service", version.ref = "paho" }
paho-client = { module = "org.eclipse.paho:org.eclipse.paho.client.mqttv3", version.ref = "paho" }
compose-bom = { module = "androidx.compose:compose-bom", version.ref = "compose-bom" }
compose-material3 = { module = "androidx.compose.material3:material3" }
compose-material-icons-extended = { module = "androidx.compose.material:material-icons-extended" }
compose-ui-tooling-preview = { module = "androidx.compose.ui:ui-tooling-preview" }
compose-ui-tooling = { module = "androidx.compose.ui:ui-tooling" }
hilt-android = { module = "com.google.dagger:hilt-android", version.ref = "hilt" }
hilt-compiler = { module = "com.google.dagger:hilt-android-compiler", version.ref = "hilt" }
hilt-navigation-compose = { module = "androidx.hilt:hilt-navigation-compose", version = "1.2.0" }
room-runtime = { module = "androidx.room:room-runtime", version.ref = "room" }
room-ktx = { module = "androidx.room:room-ktx", version.ref = "room" }
room-compiler = { module = "androidx.room:room-compiler", version.ref = "room" }
lifecycle-runtime-compose = { module = "androidx.lifecycle:lifecycle-runtime-compose", version.ref = "lifecycle" }
lifecycle-viewmodel-compose = { module = "androidx.lifecycle:lifecycle-viewmodel-compose", version.ref = "lifecycle" }
navigation-compose = { module = "androidx.navigation:navigation-compose", version.ref = "navigation" }
activity-compose = { module = "androidx.activity:activity-compose", version.ref = "activity-compose" }
coroutines-core = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-core", version.ref = "coroutines" }
coroutines-android = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-android", version.ref = "coroutines" }
junit-api = { module = "org.junit.jupiter:junit-jupiter-api", version.ref = "junit" }
junit-engine = { module = "org.junit.jupiter:junit-jupiter-engine", version.ref = "junit" }
mockk = { module = "io.mockk:mockk", version.ref = "mockk" }
coroutines-test = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-test", version.ref = "coroutines" }
turbine = { module = "app.cash.turbine:turbine", version.ref = "turbine" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
kotlin-compose = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
hilt = { id = "com.google.dagger.hilt.android", version.ref = "hilt" }
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
```

### 6.2 build.gradle.kts (module)

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.example.mqttclient"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.example.mqttclient"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"
    }
    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles("proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}

dependencies {
    implementation(libs.paho.android.service)
    implementation(libs.paho.client)
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.compose.ui.tooling.preview)
    debugImplementation(libs.compose.ui.tooling)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.navigation.compose)
    implementation(libs.activity.compose)
    implementation(libs.coroutines.core)
    implementation(libs.coroutines.android)
}
```

### 6.3 AndroidManifest.xml 关键配置

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_DATA_SYNC" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
<uses-permission android:name="android.permission.WAKE_LOCK" />

<application
    android:name=".App"
    android:theme="@style/Theme.MQTTClient"
    android:supportsRtl="true">

    <activity
        android:name=".MainActivity"
        android:exported="true"
        android:configChanges="orientation|screenSize|screenLayout|keyboardHidden"
        android:windowSoftInputMode="adjustResize">
        <intent-filter>
            <action android:name="android.intent.action.MAIN" />
            <category android:name="android.intent.category.LAUNCHER" />
        </intent-filter>
    </activity>

    <service android:name="org.eclipse.paho.android.service.MqttService" />

    <service
        android:name=".service.MqttForegroundService"
        android:foregroundServiceType="dataSync"
        android:exported="false" />

    <receiver
        android:name=".receiver.NetworkChangeReceiver"
        android:exported="false">
        <intent-filter>
            <action android:name="android.net.conn.CONNECTIVITY_CHANGE" />
        </intent-filter>
    </receiver>
</application>
```

---

## 7. 边界情况与合规

| # | 规则 |
|---|------|
| 1 | Android 13+ 首次连接时动态申请 `POST_NOTIFICATIONS`，拒绝后不崩溃，只是不显示通知 |
| 2 | 前台 Service 通知必须包含：标题 "MQTT 客户端"、内容 "{host}:{port}"、不可划除 |
| 3 | 网络切换（WiFi↔蜂窝）时自动触发重连，最多重试 3 次，间隔递增 5s→10s→30s |
| 4 | 消息列表采用分页加载（每页 50 条），防止 OOM；LazyColumn 启用 `key` 优化重组 |
| 5 | MqttManager 使用 `@Volatile` + `synchronized` 防多线程竞态，所有 MQTT 操作在 `Dispatchers.IO` |
| 6 | 证书文件支持从 `assets/` 和 SAF 文件选择器两种来源 |
| 7 | 字符串资源中英文双语（`values/strings.xml` + `values-zh/strings.xml`） |
| 8 | ProGuard 保留规则：Paho MQTT、Room、Hilt 相关类不被混淆 |
| 9 | Activity 销毁重建时通过 `SavedStateHandle` 恢复：选中的 Tab、搜索关键词、分页偏移量 |
| 10 | 应用被杀后重启，不自动连接（需用户手动操作），但加载上次使用的配置模板高亮显示 |

---

## 8. 测试

| 类型 | 测试对象 | 工具 |
|------|----------|------|
| Unit | ViewModel 状态变化、Event 处理 | JUnit 5 + MockK + Turbine |
| Unit | MqttManager 状态机 + 重连逻辑（Mock MqttClient） | MockK + Coroutines Test |
| Unit | PayloadFormatter (JSON/Hex/Base64) | JUnit 5 |
| Unit | 配置校验逻辑（端口范围、必填字段等） | JUnit 5 |
| Integration | Room DAO 所有操作 | Room in-memory DB + JUnit 5 |
| E2E | 真实 Mosquitto Broker 连接/订阅/发布 | Instrumentation Test |
