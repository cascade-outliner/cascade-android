package app.cascade.android

import android.app.Application
import android.os.Handler
import android.os.Looper
import android.util.Log
import app.cascade.android.data.Node
import app.cascade.android.data.OutlineStore
import app.cascade.android.data.Settings
import app.cascade.android.data.SqlitePersistence
import app.cascade.android.widget.TodayWidget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CascadeApplication : Application() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val handler = Handler(Looper.getMainLooper())
    private var widgetsStale = false

    lateinit var store: OutlineStore
        private set
    lateinit var settings: Settings
        private set

    override fun onCreate() {
        super.onCreate()
        settings = Settings(this)
        val persistence = SqlitePersistence(this)
        store = OutlineStore(persistence)
        scope.launch {
            val stored: List<Node> = withContext(Dispatchers.IO) {
                try {
                    persistence.load()
                } catch (error: Exception) {
                    Log.e("Cascade", "load failed, starting empty", error)
                    emptyList()
                }
            }
            store.loaded(stored)
        }
        store.addListener {
            if (!widgetsStale) {
                widgetsStale = true
                handler.post {
                    widgetsStale = false
                    TodayWidget.updateAll(this, store)
                }
            }
        }
    }

    fun whenReady(block: () -> Unit) {
        if (store.ready) {
            block()
            return
        }
        var remove = {}
        remove = store.addListener {
            if (store.ready) {
                remove()
                block()
            }
        }
    }
}
