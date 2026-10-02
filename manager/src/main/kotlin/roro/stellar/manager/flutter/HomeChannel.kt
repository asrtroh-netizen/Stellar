package roro.stellar.manager.flutter

import android.app.Activity
import android.os.Handler
import android.os.Looper
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodChannel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** 首页通道。方法名与 Dart `HomeChannel` 对齐，动作全部进 [HomeActions]。 */
class HomeChannel(
    private val activity: Activity,
    private val scope: CoroutineScope,
    private val actions: HomeActions,
) {
    private var eventSink: EventChannel.EventSink? = null
    private val main = Handler(Looper.getMainLooper())

    fun register(messenger: BinaryMessenger) {
        MethodChannel(messenger, CHANNEL).setMethodCallHandler { call, result ->
            when (call.method) {
                "getState" -> replySnapshot(result)
                "startRoot" -> run(result) { actions.startRoot() }
                "startWireless" -> run(result) { actions.startWireless() }
                "copyAdbCommand" -> run(result) { actions.copyAdbCommand() }
                "sendAdbCommand" -> run(result) { actions.sendAdbCommand() }
                "openWirelessGuide", "openAdbPermissionHelp" -> result.success(ok())
                "setBootRoot" -> run(result) {
                    actions.setBootRoot(call.argument<Boolean>("checked") == true)
                }
                "setBootWireless" -> scope.launch(Dispatchers.IO) {
                    val json = runCatching {
                        actions.setBootWireless(call.argument<Boolean>("checked") == true).toString()
                    }
                    withContext(Dispatchers.Main) {
                        json.fold({ result.success(it) }, { result.error(call.method, it.message, null) })
                    }
                }
                "setWatchdog" -> run(result) {
                    actions.setWatchdog(call.argument<Boolean>("checked") == true)
                }
                "toggleTheme" -> {
                    runCatching { actions.toggleTheme() }.fold(
                        {
                            result.success(ok())
                            activity.recreate()
                        },
                        { result.error(call.method, it.message, null) },
                    )
                }
                "setLocale" -> run(result) { actions.setLocale(call.argument<String>("tag").orEmpty()) }
                "copyText" -> run(result) { actions.copyText(call.argument<String>("text").orEmpty()) }
                "checkUpdate" -> scope.launch(Dispatchers.IO) {
                    val json = runCatching { actions.checkUpdate().toString() }
                    withContext(Dispatchers.Main) {
                        json.fold({ result.success(it) }, { result.error(call.method, it.message, null) })
                    }
                }
                else -> result.notImplemented()
            }
        }
        EventChannel(messenger, EVENTS).setStreamHandler(object : EventChannel.StreamHandler {
            override fun onListen(arguments: Any?, events: EventChannel.EventSink?) {
                eventSink = events
            }

            override fun onCancel(arguments: Any?) {
                eventSink = null
            }
        })
    }

    fun emitChanged(dartExecuting: Boolean, hostResumed: Boolean) {
        if (!dartExecuting || !hostResumed) return
        main.post { eventSink?.success("changed") }
    }

    private fun replySnapshot(result: MethodChannel.Result) {
        scope.launch(Dispatchers.IO) {
            val json = runCatching { actions.snapshot().toString() }
            withContext(Dispatchers.Main) {
                json.fold({ result.success(it) }, { result.error("getState", it.message, null) })
            }
        }
    }

    private fun run(result: MethodChannel.Result, block: () -> Unit) {
        runCatching(block).fold(
            {
                scope.launch(Dispatchers.IO) {
                    val json = runCatching { actions.snapshot().toString() }
                    withContext(Dispatchers.Main) {
                        json.fold({ result.success(it) }, { result.error("action", it.message, null) })
                    }
                }
            },
            { result.error("action", it.message, null) },
        )
    }

    private fun ok() = org.json.JSONObject().put("ok", true).toString()

    companion object {
        const val CHANNEL = "stellar/home"
        const val EVENTS = "stellar/home/events"
    }
}