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

```bash
# 安装依赖
uv init
uv add pyperclip pyautogui

# 启动服务
uv run server.py
```

服务默认监听 `0.0.0.0:9527`

### 2. Android 端配置

1. 从 [Releases](https://github.com/stefwoo/TypeFlow/releases) 下载 APK 并安装
2. 打开 App，在顶部输入框配置 PC 的 IP 地址和端口
   - 格式：`192.168.1.100:9527`
   - IP 地址可通过 `ipconfig` (Windows) 或 `ifconfig` (Linux) 查看
3. 在中间文本框输入或粘贴文字
4. 点击底部「发送」按钮

### 3. 使用流程

1. 在 PC 端打开目标输入框（记事本、浏览器搜索框等）
2. 在手机端使用系统语音输入大段文字
3. 点击发送，文字自动复制到剪贴板并在 PC 端上屏
4. 手机端输入框自动清空，可继续输入下一段

## 项目结构

```
TypeFlow/
├── server.py              # PC 端 Python HTTP 服务
├── app/
│   ├── src/main/
│   │   ├── java/com/remoteinput/
│   │   │   └── MainActivity.kt   # Android 主界面
│   │   └── res/                  # 资源文件
│   └── build.gradle
└── .github/workflows/
    └── android.yml        # CI/CD 自动构建
```

## 技术栈

- **PC 端**: Python + http.server + pyperclip + pyautogui
- **Android 端**: Kotlin + Jetpack Compose + Material 3
- **构建**: GitHub Actions

## 许可证

MIT
