# 远程极简输入法 (Remote Input)

利用手机系统自带的高效语音输入，将文字一键发送到电脑端光标处上屏。

## 目的

解决在电脑上输入大段中文文本时的痛点：
- 手机语音输入识别率高、体验好
- 避免电脑端输入法候选词、拼音中间状态的同步冲突
- 极简架构：HTTP POST + 剪贴板粘贴上屏

## 系统架构

```
┌─────────────────┐     HTTP POST      ┌─────────────────┐
│   Android 端    │ ──────────────────▶│     PC 端       │
│ (语音/手写输入) │   {"text": "..."}   │ (HTTP 服务)     │
└─────────────────┘                    └────────┬────────┘
                                                 │
                                         写入剪贴板 + Ctrl+V
                                                 ▼
                                         ┌─────────────────┐
                                         │  目标输入框上屏  │
                                         └─────────────────┘
```

## 使用方法

### 1. PC 端配置

从 [Releases](https://github.com/stefwoo/TypeFlow/releases) 下载对应平台的二进制文件：

- **Windows**: `typeflow-windows-amd64.exe`
- **Linux**: `typeflow-linux-amd64`
- **macOS**: `typeflow-darwin-amd64`

#### 启动方式

```bash
# 默认端口 9527
./typeflow-windows-amd64.exe

# 自定义端口
./typeflow-windows-amd64.exe --port 8080
```

#### 开机自启动（Windows，推荐任务计划程序）

**推荐：任务计划程序**（无 UAC 提示，一劳永逸）

1. 将 `typeflow-windows-amd64.exe` 放到合适位置（如 `C:\Program Files\TypeFlow\`）
2. 打开「任务计划程序」(taskschd.msc)
3. 创建基本任务：
   - 名称：`TypeFlow`
   - 触发器：选择「计算机启动」或「登录时」
   - 操作：启动程序 → 选择 exe 文件
4. 完成

或者以管理员运行 CMD：
```cmd
schtasks /create /tn "TypeFlow" /tr "C:\Program Files\TypeFlow\typeflow.exe" /sc onlogon /rl limited
```

---

**注意：不推荐使用 Startup 文件夹**，因为每次开机都会弹出 UAC 确认对话框。

#### Python 版本（备选）

```bash
# 直接运行（自动安装依赖）
uv run server.py
```

### 2. Android 端配置

1. 从 [Releases](https://github.com/stefwoo/TypeFlow/releases) 下载 APK 并安装
2. 打开 App，点击右上角 ⚙️ 设置按钮
3. 添加服务器配置：
   - 名称：如「办公室」、「家里」
   - 地址：如 `192.168.1.100:9527`
4. 可添加多台电脑，随时切换

### 3. 使用流程

1. 确保 PC 端程序已运行（开机自启）
2. 在手机端 TypeFlow App 中输入文字（可使用系统语音输入）
3. 点击「发送」按钮
4. 在 PC 端目标输入框中，文字自动粘贴上屏

## 项目结构

```
TypeFlow/
├── server.py              # Python HTTP 服务
├── cmd/server/
│   └── main.go           # Go HTTP 服务
├── app/
│   ├── src/main/
│   │   ├── java/com/remoteinput/
│   │   │   └── MainActivity.kt   # Android 主界面
│   │   └── res/                  # 资源文件
│   └── build.gradle
├── go.mod                 # Go 依赖
└── .github/workflows/
    └── android.yml        # CI/CD 自动构建
```

## 技术栈

- **PC 端 (Python)**: Python + http.server + pyperclip + pyautogui
- **PC 端 (Go)**: Go + atotto/clipboard
- **Android 端**: Kotlin + Jetpack Compose + Material 3
- **构建**: GitHub Actions

## 许可证

MIT
