package roro.stellar.manager.flutter

import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodChannel
import org.json.JSONObject

/**
 * 应用 / 设置 / 终端 / 配对还没从 Compose 迁过来。
 * 先把通道接上，避免 Dart 调空插件；页面能打开，动作返回空快照。
 */
object StubChannels {
    fun register(messenger: BinaryMessenger) {
        listOf("stellar/apps", "stellar/settings", "stellar/terminal", "stellar/pairing").forEach { name ->
            MethodChannel(messenger, name).setMethodCallHandler { _, result ->
                result.success(JSONObject().put("ok", true).put("unavailable", true).toString())
            }
        }
    }
}