package roro.stellar.manager.watchdog

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.IBinder
import android.util.Log
import roro.stellar.Stellar
import roro.stellar.manager.AppConstants
import roro.stellar.manager.R
import roro.stellar.manager.StellarSettings
import roro.stellar.manager.compat.BuildUtils.atLeast34
import roro.stellar.manager.startup.worker.AdbStartWorker

/**
 * ADB 会话的断线守护。只在「开关打开 + 上次是 ADB 启动」时工作。
 * 服务挂掉后交给 Stellar 自己的 [AdbStartWorker] 重新拉起，不另写一套启动器。
 * 最多重试 5 次，避免无线调试不可用时死循环。
 */
class AdbWatchdogService : Service() {

    private var attempts = 0
    private var listening = false

    private val onDead = Stellar.OnBinderDeadListener {
        if (!eligible()) {
            stopSelf()
            return@OnBinderDeadListener
        }
        if (attempts >= MAX_ATTEMPTS) {
            Log.w(AppConstants.TAG, "ADB watchdog reached restart limit")
            stopSelf()
            return@OnBinderDeadListener
        }
        attempts += 1
        Log.i(AppConstants.TAG, "ADB watchdog restart $attempts/$MAX_ATTEMPTS")
        AdbStartWorker.enqueue(this)
    }

    override fun onCreate() {
        super.onCreate()
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notification_channel_watchdog),
                NotificationManager.IMPORTANCE_LOW
            )
        )
        val notification = notification()
        if (atLeast34) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP || !eligible()) {
            stopSelf()
            return START_NOT_STICKY
        }
        if (!listening) {
            Stellar.addBinderDeadListener(onDead)
            listening = true
        }
        if (Stellar.pingBinder()) attempts = 0
        return START_STICKY
    }

    override fun onDestroy() {
        if (listening) {
            runCatching { Stellar.removeBinderDeadListener(onDead) }
            listening = false
        }
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun notification(): Notification =
        Notification.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stellar)
            .setContentTitle(getString(R.string.notification_watchdog_title))
            .setContentText(getString(R.string.notification_watchdog_content))
            .setOngoing(true)
            .build()

    companion object {
        private const val ACTION_STOP = "roro.stellar.manager.watchdog.STOP"
        private const val CHANNEL_ID = "adb_watchdog"
        private const val NOTIFICATION_ID = AppConstants.NOTIFICATION_ID_STATUS + 2
        private const val MAX_ATTEMPTS = 5

        fun sync(context: Context) {
            if (eligible()) {
                context.startForegroundService(Intent(context, AdbWatchdogService::class.java))
            } else {
                context.stopService(
                    Intent(context, AdbWatchdogService::class.java).setAction(ACTION_STOP)
                )
            }
        }

        private fun eligible(): Boolean =
            StellarSettings.getPreferences()
                .getBoolean(StellarSettings.WATCHDOG_ENABLED_ADB, false)
                && StellarSettings.getLastLaunchMethod() == StellarSettings.LaunchMethod.ADB
    }
}