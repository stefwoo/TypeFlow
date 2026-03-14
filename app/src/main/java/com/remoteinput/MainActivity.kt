package com.remoteinput

import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import android.content.Context

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RemoteInputApp()
        }
    }
}

data class ServerConfig(
    val name: String,
    val address: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemoteInputApp() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var servers by remember {
        mutableStateOf(loadServers(context))
    }
    var selectedServer by remember {
        mutableStateOf(loadSelectedServer(context))
    }
    var inputText by remember { mutableStateOf("") }
    var isSending by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf("") }
    var showSettings by remember { mutableStateOf(false) }
    var textFieldFocusRequester by remember { FocusRequester() }

    // 启动时自动弹出输入法
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(300)
        textFieldFocusRequester.requestFocus()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 顶部标题栏
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "TypeFlow",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.primary
            )
            
            if (selectedServer != null) {
                Text(
                    text = selectedServer!!.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            IconButton(onClick = { showSettings = true }) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "设置"
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 大号输入框 - 带圆角
        OutlinedTextField(
            value = inputText,
            onValueChange = { inputText = it },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .focusRequester(textFieldFocusRequester),
            placeholder = { Text("点击输入文字...") },
            textStyle = LocalTextStyle.current.copy(fontSize = 18.sp),
            shape = RoundedCornerShape(16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 发送按钮
        Button(
            onClick = {
                if (inputText.isBlank()) {
                    statusMessage = "输入内容为空"
                    return@Button
                }
                if (selectedServer == null) {
                    statusMessage = "请先设置 PC 地址"
                    return@Button
                }

                isSending = true
                statusMessage = "发送中..."

                scope.launch {
                    val result = sendTextToPC(selectedServer!!.address, inputText)
                    if (result) {
                        statusMessage = "发送成功 ✓"
                        inputText = ""
                    } else {
                        statusMessage = "发送失败 ✗"
                    }
                    isSending = false
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = !isSending
        ) {
            Text(if (isSending) "发送中..." else "发送", style = MaterialTheme.typography.titleMedium)
        }

        if (statusMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = statusMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = if (statusMessage.contains("成功")) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.error,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }

    // 设置对话框
    if (showSettings) {
        SettingsDialog(
            servers = servers,
            selectedServer = selectedServer,
            onServersChange = { newServers ->
                servers = newServers
                saveServers(context, newServers)
            },
            onSelectServer = { server ->
                selectedServer = server
                saveSelectedServer(context, server)
            },
            onDismiss = { showSettings = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDialog(
    servers: List<ServerConfig>,
    selectedServer: ServerConfig?,
    onServersChange: (List<ServerConfig>) -> Unit,
    onSelectServer: (ServerConfig) -> Unit,
    onDismiss: () -> Unit
) {
    var newName by remember { mutableStateOf("") }
    var newAddress by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("设置服务器") },
        text = {
            Column {
                // 当前服务器显示
                if (selectedServer != null) {
                    Text(
                        text = "当前: ${selectedServer.name} (${selectedServer.address})",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // 服务器列表
                servers.forEach { server ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = { onSelectServer(server) }
                        ) {
                            Text(
                                text = "${server.name} (${server.address})",
                                color = if (server == selectedServer) 
                                    MaterialTheme.colorScheme.primary 
                                else 
                                    MaterialTheme.colorScheme.onSurface
                            )
                        }
                        TextButton(
                            onClick = {
                                val newList = servers.filter { it != server }
                                onServersChange(newList)
                                if (server == selectedServer && newList.isNotEmpty()) {
                                    onSelectServer(newList.first())
                                }
                            }
                        ) {
                            Text("删除", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 添加新服务器
                Text("添加新服务器:", style = MaterialTheme.typography.labelMedium)
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("名称(如: 办公室)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = newAddress,
                    onValueChange = { newAddress = it },
                    label = { Text("IP:Port (如: 192.168.1.100:9527)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        if (newName.isNotBlank() && newAddress.isNotBlank()) {
                            val newServer = ServerConfig(newName, newAddress)
                            val newList = servers + newServer
                            onServersChange(newList)
                            onSelectServer(newServer)
                            newName = ""
                            newAddress = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("添加并使用")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("关闭")
            }
        }
    )
}

suspend fun sendTextToPC(address: String, text: String): Boolean {
    return withContext(Dispatchers.IO) {
        try {
            val fullUrl = "http://$address/"
            val url = URL(fullUrl)
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.setRequestProperty("Accept", "application/json")
            conn.doOutput = true
            conn.doInput = true
            conn.connectTimeout = 10000
            conn.readTimeout = 10000

            val jsonBody = """{"text": ${escapeJson(text)}}"""

            conn.outputStream.use { outputStream ->
                outputStream.write(jsonBody.toByteArray(Charsets.UTF_8))
            }

            val responseCode = conn.responseCode
            conn.disconnect()

            responseCode == 200
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}

fun escapeJson(text: String): String {
    return text
        .replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("\n", "\\n")
        .replace("\r", "\\r")
        .replace("\t", "\\t")
        .let { "\"$it\"" }
}

private const val PREFS_NAME = "remote_input_prefs"
private const val KEY_SERVERS = "servers"
private const val KEY_SELECTED_SERVER = "selected_server"

fun loadServers(context: Context): List<ServerConfig> {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val serversJson = prefs.getString(KEY_SERVERS, "") ?: ""
    if (serversJson.isBlank()) return emptyList()
    
    return try {
        serversJson.split("|").mapNotNull { item ->
            val parts = item.split("::")
            if (parts.size == 2) ServerConfig(parts[0], parts[1]) else null
        }
    } catch (e: Exception) {
        emptyList()
    }
}

fun saveServers(context: Context, servers: List<ServerConfig>) {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val serversJson = servers.joinToString("|") { "${it.name}::${it.address}" }
    prefs.edit().putString(KEY_SERVERS, serversJson).apply()
}

fun loadSelectedServer(context: Context): ServerConfig? {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val selectedJson = prefs.getString(KEY_SELECTED_SERVER, "") ?: ""
    if (selectedJson.isBlank()) return null
    
    return try {
        val parts = selectedJson.split("::")
        if (parts.size == 2) ServerConfig(parts[0], parts[1]) else null
    } catch (e: Exception) {
        null
    }
}

fun saveSelectedServer(context: Context, server: ServerConfig?) {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    if (server != null) {
        prefs.edit().putString(KEY_SELECTED_SERVER, "${server.name}::${server.address}").apply()
    } else {
        prefs.edit().remove(KEY_SELECTED_SERVER).apply()
    }
}
