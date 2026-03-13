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

#### 方式一：Go 版本（推荐，支持 Windows 服务）

从 [Releases](https://github.com/stefwoo/TypeFlow/releases) 下载对应平台的二进制文件：

- **Windows**: `typeflow-windows-amd64.exe`
- **Linux**: `typeflow-linux-amd64`
- **macOS**: `typeflow-darwin-amd64`

直接运行：
```bash
./typeflow-windows-amd64.exe
```

服务默认监听 `0.0.0.0:9527`

#### 方式二：Python 版本

```bash
# 直接运行（自动安装依赖）
uv run server.py
```

### 2. 注册为 Windows 服务

#### 方式一：使用 NSSM（推荐）

1. 下载 [NSSM](https://nssm.cc/download)
2. 将 `typeflow-windows-amd64.exe` 放到合适的位置（如 `C:\Program Files\TypeFlow\`）
3. 以管理员身份打开 CMD：
```cmd
nssm install TypeFlow "C:\Program Files\TypeFlow\typeflow-windows-amd64.exe"
nssm start TypeFlow
```

#### 方式二：使用 Go 内置服务（需要代码支持）

> 注意：此方式需要重新编译，代码位于 `cmd/server/main.go`

使用 `golang.org/x/sys/windows/svc` 可实现无第三方依赖的 Windows 服务。

### 3. Android 端配置

1. 从 [Releases](https://github.com/stefwoo/TypeFlow/releases) 下载 APK 并安装
2. 打开 App，在顶部输入框配置 PC 的 IP 地址和端口
   - 格式：`192.168.1.100:9527`
   - IP 地址可通过 `ipconfig` (Windows) 或 `ifconfig` (Linux) 查看
3. 在中间文本框输入或粘贴文字
4. 点击底部「发送」按钮

### 4. 使用流程

1. 在 PC 端打开目标输入框（记事本、浏览器搜索框等）
2. 在手机端使用系统语音输入大段文字
3. 点击发送，文字自动复制到剪贴板并在 PC 端上屏
4. 手机端输入框自动清空，可继续输入下一段

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
- **PC 端 (Go)**: Go + atotto/clipboard + robotgo
- **Android 端**: Kotlin + Jetpack Compose + Material 3
- **构建**: GitHub Actions

## 许可证

MIT
