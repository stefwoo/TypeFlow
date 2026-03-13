package com.remoteinput

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
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

@Composable
fun RemoteInputApp() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var serverAddress by remember {
        mutableStateOf(loadServerAddress(context))
    }
    var inputText by remember { mutableStateOf("") }
    var isSending by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = serverAddress,
            onValueChange = {
                serverAddress = it
                saveServerAddress(context, it)
            },
            label = { Text("PC IP:Port (如 192.168.1.100:9527)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = inputText,
            onValueChange = { inputText = it },
            label = { Text("输入文本") },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            minLines = 3
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (inputText.isBlank()) {
                    statusMessage = "输入内容为空"
                    return@Button
                }
                if (serverAddress.isBlank()) {
                    statusMessage = "请先配置 PC IP 地址"
                    return@Button
                }

                isSending = true
                statusMessage = "发送中..."

                scope.launch {
                    val result = sendTextToPC(serverAddress, inputText)
                    if (result) {
                        statusMessage = "发送成功"
                        inputText = ""
                    } else {
                        statusMessage = "发送失败"
                    }
                    isSending = false
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            enabled = !isSending
        ) {
            Text(if (isSending) "发送中..." else "发送", style = MaterialTheme.typography.titleMedium)
        }

        if (statusMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(statusMessage, color = MaterialTheme.colorScheme.primary)
        }
    }
}

suspend fun sendTextToPC(address: String, text: String): Boolean {
    return withContext(Dispatchers.IO) {
        try {
            val fullUrl = "http://$address"
            val url = URL(fullUrl)
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.doOutput = true

            val jsonBody = """{"text": ${escapeJson(text)}}"""

            val outputStream: OutputStream = conn.outputStream
            outputStream.write(jsonBody.toByteArray())
            outputStream.flush()
            outputStream.close()

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
private const val KEY_SERVER_ADDRESS = "server_address"

fun loadServerAddress(context: Context): String {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    return prefs.getString(KEY_SERVER_ADDRESS, "") ?: ""
}

fun saveServerAddress(context: Context, address: String) {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    prefs.edit().putString(KEY_SERVER_ADDRESS, address).apply()
}
