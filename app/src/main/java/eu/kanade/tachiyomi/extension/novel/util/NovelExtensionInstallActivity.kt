package eu.kanade.tachiyomi.extension.novel.util

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import eu.kanade.tachiyomi.extension.InstallStep
import eu.kanade.tachiyomi.extension.novel.NovelExtensionManager
import eu.kanade.tachiyomi.util.system.hasMiuiPackageInstaller
import eu.kanade.tachiyomi.util.system.toast
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get
import kotlin.time.Duration.Companion.seconds

class NovelExtensionInstallActivity : Activity() {

    private var ignoreUntil = 0L
    private var ignoreResult = false
    private var hasIgnoredResult = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        @Suppress("DEPRECATION")
        val installIntent = Intent(Intent.ACTION_INSTALL_PACKAGE)
            .setDataAndType(intent.data, intent.type)
            .putExtra(Intent.EXTRA_RETURN_RESULT, true)
            .setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        if (hasMiuiPackageInstaller) {
            ignoreResult = true
            ignoreUntil = System.nanoTime() + 1.seconds.inWholeNanoseconds
        }

        try {
            startActivityForResult(installIntent, INSTALL_REQUEST_CODE)
        } catch (error: Exception) {
            toast(error.message)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (ignoreResult && System.nanoTime() < ignoreUntil) {
            hasIgnoredResult = true
            return
        }
        if (requestCode == INSTALL_REQUEST_CODE) {
            checkInstallationResult(resultCode)
        }
        finish()
    }

    override fun onStart() {
        super.onStart()
        if (hasIgnoredResult) {
            checkInstallationResult(RESULT_CANCELED)
            finish()
        }
    }

    private fun checkInstallationResult(resultCode: Int) {
        val downloadId = intent.extras!!.getLong(NovelExtensionInstaller.EXTRA_DOWNLOAD_ID)
        val extensionManager = Injekt.get<NovelExtensionManager>()
        val newStep = when (resultCode) {
            RESULT_OK -> InstallStep.Installed
            RESULT_CANCELED -> InstallStep.Idle
            else -> InstallStep.Error
        }
        extensionManager.updateInstallStep(downloadId, newStep)
    }
}

private const val INSTALL_REQUEST_CODE = 600
