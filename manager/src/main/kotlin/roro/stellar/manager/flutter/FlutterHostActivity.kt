package roro.stellar.manager.flutter

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.Lifecycle
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import roro.stellar.Stellar
import roro.stellar.manager.receiver.StellarReceiverStarter

/**
 * Flutter 壳宿主。底栏和玻璃在 Dart；启动、授权、特权服务仍走原来的 Kotlin。
 */
class FlutterHostActivity : FlutterActivity() {

    private val ioScope = CoroutineScope(Dispatchers.IO)
    private lateinit var actions: HomeActions
    private lateinit var homeChannel: HomeChannel

    private val binderReceivedListener = Stellar.OnBinderReceivedListener { emitHomeChanged() }
    private val binderDeadListener = Stellar.OnBinderDeadListener { emitHomeChanged() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!::actions.isInitialized) actions = HomeActions(this)
        if (intent?.getBooleanExtra(EXTRA_START_SERVICE_VIA_WADB, false) == true) {
            StellarReceiverStarter.start(this, forceStart = true)
        }
        Stellar.addBinderReceivedListenerSticky(binderReceivedListener)
        Stellar.addBinderDeadListener(binderDeadListener)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent.getBooleanExtra(EXTRA_START_SERVICE_VIA_WADB, false)) {
            StellarReceiverStarter.start(this, forceStart = true)
        }
    }

    override fun getInitialRoute(): String? {
        val tab = intent?.getIntExtra(EXTRA_TAB, -1) ?: -1
        if (tab < 0) return super.getInitialRoute()
        intent.removeExtra(EXTRA_TAB)
        return "/tab/${if (tab == 1) 1 else 0}"
    }

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        if (!::actions.isInitialized) actions = HomeActions(this)
        val messenger = flutterEngine.dartExecutor.binaryMessenger
        homeChannel = HomeChannel(this, ioScope, actions)
        homeChannel.register(messenger)
        StubChannels.register(messenger)
    }

    override fun onDestroy() {
        ioScope.cancel()
        Stellar.removeBinderReceivedListener(binderReceivedListener)
        Stellar.removeBinderDeadListener(binderDeadListener)
        super.onDestroy()
    }

    private fun emitHomeChanged() {
        if (!::homeChannel.isInitialized) return
        homeChannel.emitChanged(
            dartExecuting = flutterEngine?.dartExecutor?.isExecutingDart == true,
            hostResumed = lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED),
        )
    }

    companion object {
        const val EXTRA_TAB = "roro.stellar.manager.extra.TAB"
        const val EXTRA_START_SERVICE_VIA_WADB = "roro.stellar.manager.extra.START_SERVICE_VIA_WADB"
    }
}