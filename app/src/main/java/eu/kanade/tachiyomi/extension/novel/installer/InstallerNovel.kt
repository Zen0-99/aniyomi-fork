package eu.kanade.tachiyomi.extension.novel.installer

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import androidx.annotation.CallSuper
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import eu.kanade.tachiyomi.extension.InstallStep
import eu.kanade.tachiyomi.extension.novel.NovelExtensionManager
import uy.kohesive.injekt.injectLazy
import java.util.Collections
import java.util.concurrent.atomic.AtomicReference

abstract class InstallerNovel(private val service: Service) {

    private val extensionManager: NovelExtensionManager by injectLazy()

    private var waitingInstall = AtomicReference<Entry>(null)
    private val queue = Collections.synchronizedList(mutableListOf<Entry>())

    private val cancelReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val downloadId = intent.getLongExtra(EXTRA_DOWNLOAD_ID, -1).takeIf { it >= 0 } ?: return
            cancelQueue(downloadId)
        }
    }

    abstract var ready: Boolean

    fun addToQueue(downloadId: Long, uri: Uri) {
        queue.add(Entry(downloadId, uri))
        checkQueue()
    }

    @CallSuper
    open fun processEntry(entry: Entry) {
        extensionManager.setInstalling(entry.downloadId)
    }

    open fun cancelEntry(entry: Entry): Boolean {
        return true
    }

    fun continueQueue(resultStep: InstallStep) {
        val completedEntry = waitingInstall.getAndSet(null)
        if (completedEntry != null) {
            extensionManager.updateInstallStep(completedEntry.downloadId, resultStep)
            checkQueue()
        }
    }

    fun checkQueue() {
        if (!ready) {
            return
        }
        if (queue.isEmpty()) {
            service.stopSelf()
            return
        }
        val nextEntry = queue.first()
        if (waitingInstall.compareAndSet(null, nextEntry)) {
            queue.removeAt(0)
            processEntry(nextEntry)
        }
    }

    @CallSuper
    open fun onDestroy() {
        LocalBroadcastManager.getInstance(service).unregisterReceiver(cancelReceiver)
        queue.forEach { extensionManager.updateInstallStep(it.downloadId, InstallStep.Error) }
        queue.clear()
        waitingInstall.set(null)
    }

    protected fun getActiveEntry(): Entry? = waitingInstall.get()

    private fun cancelQueue(downloadId: Long) {
        val waitingInstall = this.waitingInstall.get()
        val toCancel = queue.find { it.downloadId == downloadId } ?: waitingInstall ?: return
        if (cancelEntry(toCancel)) {
            queue.remove(toCancel)
            if (waitingInstall == toCancel) {
                this.waitingInstall.set(null)
                checkQueue()
            }
            extensionManager.updateInstallStep(downloadId, InstallStep.Idle)
        }
    }

    data class Entry(val downloadId: Long, val uri: Uri)

    init {
        val filter = IntentFilter(ACTION_CANCEL_QUEUE)
        LocalBroadcastManager.getInstance(service).registerReceiver(cancelReceiver, filter)
    }

    companion object {
        private const val ACTION_CANCEL_QUEUE = "InstallerNovel.action.CANCEL_QUEUE"
        private const val EXTRA_DOWNLOAD_ID = "InstallerNovel.extra.DOWNLOAD_ID"

        fun cancelInstallQueue(context: Context, downloadId: Long) {
            val intent = Intent(ACTION_CANCEL_QUEUE)
            intent.putExtra(EXTRA_DOWNLOAD_ID, downloadId)
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
        }
    }
}
