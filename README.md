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

#### 开机自启动（Windows）

将 `typeflow-windows-amd64.exe` 复制到以下位置：

1. **方法一：Startup 文件夹**
   - 按 `Win + R`，输入 `shell:startup`
   - 将 exe 文件粘贴到打开的文件夹中

2. **方法二：任务计划程序**
   - 打开「任务计划程序」
   - 创建基本任务 → 设置开机触发 → 添加 exe 路径

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
