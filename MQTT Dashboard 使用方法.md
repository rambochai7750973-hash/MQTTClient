# MQTT Dashboard (安卓 App) 使用方法

## 1️⃣ 下载安装

- Google Play 搜索 **MQTT Dashboard**（蓝色圆形图标）
- 或从 APKPure / 酷安下载 APK

## 2️⃣ 新建连接

```
打开 App → 点击右上角 "+" 按钮 → 填写连接信息：
```

### 连接配置表

| 字段 | 填写内容 | 说明 |
|------|----------|------|
| **Name** | `ESP8266 监控` | 自定义名称 |
| **Protocol** | `WebSocket (WSS)` | **关键！必须选这个** |
| **Host** | `mqtt-websocket-bridge.你的子域名.workers.dev` | 你的 Worker 地址 |
| **Port** | `443` | Cloudflare 标准端口 |
| **Path** | `/mqtt` | **必须填写 /mqtt** |
| **Client ID** | `android_001` | 唯一标识，建议加随机后缀 |
| **Username** | (留空) | MQTT Broker 用户名（如有） |
| **Password** | (留空) | MQTT Broker 密码（如有） |

### 配置截图示意

```
┌─────────────────────────────────────┐
│  ☰  New Connection                  │
├─────────────────────────────────────┤
│  Name        [ ESP8266 监控       ] │
│  Protocol    [ WebSocket (WSS)  ▼ ] │  ← 选这个
│  Host        [ mqtt-websocket-br ] │
│              [ idge.xxx.workers.d ] │
│              [ ev                 ] │
│  Port        [ 443               ] │
│  Path        [ /mqtt             ] │  ← 必须填
│  Client ID   [ android_001       ] │
│  Username    [                   ] │
│  Password    [                   ] │
│                                     │
│  [ SAVE ]     [ CANCEL ]            │
└─────────────────────────────────────┘
```

## 3️⃣ 连接测试

```
1. 填写完配置 → 点击 SAVE
2. 在连接列表点击刚创建的 "ESP8266 监控"
3. 顶部状态条变为 绿色 ✔  = 连接成功
4. 状态条红色 ✘  = 连接失败，检查配置
```

## 4️⃣ 订阅主题（接收 ESP8266 数据）

```
连接成功后 → 进入主界面：

1. 点击顶部 "Subscribe" 标签页
2. 点击右下角 "+" 按钮

┌─────────────────────────────────────┐
│  Subscribe to Topic                 │
├─────────────────────────────────────┤
│  Topic Filter [ sensor/#         ] │  ← 用 # 通配符
│  Color       [ 绿色              ] │
│  QoS         [ 0                 ] │
│                                     │
│  [ SUBSCRIBE ]                      │
└─────────────────────────────────────┘

3. 点击 SUBSCRIBE
4. 此时 ESP8266 发布的数据会实时显示在列表中

   ┌─────────────────────────────────────┐
   │  15:30:01  sensor/temp    25.5°C   │
   │  15:30:06  sensor/temp    25.7°C   │
   │  15:30:11  sensor/temp    25.3°C   │
   └─────────────────────────────────────┘
```

## 5️⃣ 发布消息（控制 ESP8266）

```
1. 点击顶部 "Publish" 标签页
2. 输入 Topic 和 Payload

┌─────────────────────────────────────┐
│  Publish Message                    │
├─────────────────────────────────────┤
│  Topic    [ esp8266/control      ] │
│  Payload  [ relay_on             ] │
│  QoS      [ 1                    ] │
│  Retain   [ ☐                    ] │
│                                     │
│  [ PUBLISH ]                        │
└─────────────────────────────────────┘

3. 点击 PUBLISH → ESP8266 收到消息执行动作
```

## 6️⃣ 创建 Dashboard 监控面板

```
在 Dashboard 标签页添加控件，可视化显示传感器数据：

1. 点击 "Dashboard" 标签页
2. 点击右下角 "+" → 选择控件类型：

   ┌─────────────────────────────────────┐
   │  Add Widget                         │
   ├─────────────────────────────────────┤
   │  ● Gauge        (仪表盘)           │
   │  ○ Text         (文本框)           │
   │  ○ Button       (按钮)             │
   │  ○ Switch       (开关)             │
   │  ○ Chart        (图表)             │
   │  ○ LED          (指示灯)           │
   └─────────────────────────────────────┘

   以 Gauge 为例：
   ┌─────────────────────────────────────┐
   │  Gauge Configuration                │
   ├─────────────────────────────────────┤
   │  Name          [ 温度             ] │
   │  Topic         [ sensor/temp     ] │
   │  Unit          [ °C              ] │
   │  Min Value     [ 0               ] │
   │  Max Value     [ 50              ] │
   │  Precision     [ 1               ] │
   │                                     │
   │  [ SAVE ]                           │
   └─────────────────────────────────────┘
```

## 7️⃣ 完整链路测试

```
  ESP8266                          MQTT Dashboard (安卓)
    │                                     │
    │── publish sensor/temp 25.5 ──────►  │
    │                                     │
    │── publish sensor/hum   80%   ──────► │
    │                                     │
    │◄── subscribe esp8266/control  ──────│
    │    (收到 relay_on)                  │
    │                                     │
```

## 8️⃣ 常见问题

| 问题 | 原因 | 解决 |
|------|------|------|
| 连接红色失败 | 协议没选 WSS | Protocol 改为 WebSocket (WSS) |
| 连接红色失败 | Path 没填 | 必须填写 /mqtt |
| 连接红色失败 | Host 填错 | 检查 Worker 地址是否正确 |
| 能连但收不到数据 | Topic 拼写错误 | 检查 ESP8266 发布的 Topic |
| 数据乱码 | 编码问题 | Payload 用纯文本，不要用二进制 |

## 9️⃣ 注意事项

- ⚠️ **手机和 ESP8266 必须在同一个 MQTT Broker 下通信**
- 默认使用公共 Broker `broker.emqx.io`，所有数据公开
- 生产环境建议部署私有 Broker 并开启鉴权
- MQTT Dashboard 免费版有数量限制，但个人使用完全足够
