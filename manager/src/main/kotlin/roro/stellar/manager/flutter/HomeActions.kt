package roro.stellar.manager.flutter

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.content.edit
import com.topjohnwu.superuser.Shell
import org.json.JSONArray
import org.json.JSONObject
import roro.stellar.Stellar
import roro.stellar.manager.StellarSettings
import roro.stellar.manager.compat.ClipboardUtils
import roro.stellar.manager.receiver.StellarReceiverStarter
import roro.stellar.manager.startup.command.Starter
import roro.stellar.manager.startup.worker.AdbStartWorker
import roro.stellar.manager.ui.theme.ThemeMode
import roro.stellar.manager.ui.theme.ThemePreferences

/**
 * 首页动作全部留在 Kotlin。Dart 只拿 JSON 快照。
 * 启动路径与 [StellarReceiverStarter] 相同：Root 走 libsu，ADB 走 [AdbStartWorker]。
 */
class HomeActions(private val context: Context) {

    fun snapshot(): JSONObject {
        val prefs = StellarSettings.getPreferences()
        val running = Stellar.pingBinder()
        val rooted = Shell.getShell().isRoot
        val boot = StellarSettings.getBootMode()
        val dark = when (ThemePreferences.themeMode.value) {
            ThemeMode.DARK -> true
            ThemeMode.LIGHT -> false
            ThemeMode.AUTO -> {
                val night = context.resources.configuration.uiMode and
                    android.content.res.Configuration.UI_MODE_NIGHT_MASK
                night == android.content.res.Configuration.UI_MODE_NIGHT_YES
            }
        }
        return JSONObject()
            .put("running", running)
            .put("permission", running)
            .put("grantedCount", if (running) 0 else -1)
            .put("rooted", rooted)
            .put("rootRestart", running && StellarSettings.getLastLaunchMethod() == StellarSettings.LaunchMethod.ROOT)
            .put("showWireless", true)
            .put("showPair", Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
            .put("bootRoot", boot == StellarSettings.BootMode.SCRIPT)
            .put("bootWireless", boot == StellarSettings.BootMode.BROADCAST || boot == StellarSettings.BootMode.TCPIP_PREWARM)
            .put("watchdog", prefs.getBoolean(StellarSettings.WATCHDOG_ENABLED_ADB, false))
            .put("dark", dark)
            .put("adbLimited", false)
            .put("adbCommand", Starter.adbCommand)
            .put("appsSub", if (running) "Service is running" else "Stellar is not running")
            .put("rootSub", if (rooted) "Root available" else "Root required")
            .put("locales", JSONArray())
            .put("copy", JSONObject())
    }

    fun startRoot() {
        if (!Shell.getShell().isRoot) return
        val ok = Shell.cmd(Starter.internalCommand).exec().code == 0
        if (ok) StellarSettings.setLastLaunchMethod(StellarSettings.LaunchMethod.ROOT)
    }

    fun startWireless() {
        AdbStartWorker.enqueue(context)
        StellarSettings.setLastLaunchMethod(StellarSettings.LaunchMethod.ADB)
    }

    fun copyAdbCommand() {
        ClipboardUtils.put(context, Starter.adbCommand)
    }

    fun sendAdbCommand() {
        val send = Intent(Intent.ACTION_SEND)
            .setType("text/plain")
            .putExtra(Intent.EXTRA_TEXT, Starter.adbCommand)
        context.startActivity(Intent.createChooser(send, Starter.adbCommand).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    fun setBootRoot(checked: Boolean) {
        StellarSettings.setBootMode(
            if (checked) StellarSettings.BootMode.SCRIPT else StellarSettings.BootMode.NONE,
        )
    }

    fun setBootWireless(checked: Boolean): JSONObject {
        StellarSettings.setBootMode(
            if (checked) StellarSettings.BootMode.BROADCAST else StellarSettings.BootMode.NONE,
        )
        return JSONObject().put("ok", true).put("state", snapshot())
    }

    fun setWatchdog(checked: Boolean) {
        StellarSettings.getPreferences().edit {
            putBoolean(StellarSettings.WATCHDOG_ENABLED_ADB, checked)
        }
    }

    fun toggleTheme() {
        val next = if (ThemePreferences.themeMode.value == ThemeMode.DARK) ThemeMode.LIGHT else ThemeMode.DARK
        ThemePreferences.setThemeMode(next)
    }

    fun copyText(text: String) {
        ClipboardUtils.put(context, text)
    }

    /** 语言列表还在 Compose 设置里，Flutter 壳先不重建 Activity。 */
    fun setLocale(@Suppress("UNUSED_PARAMETER") tag: String) = Unit

    fun checkUpdate(): JSONObject = JSONObject()
        .put("ok", true)
        .put("hasUpdate", false)
        .put("message", "")

    fun handleStartViaWadb(start: Boolean) {
        if (start) StellarReceiverStarter.start(context, forceStart = true)
    }
}