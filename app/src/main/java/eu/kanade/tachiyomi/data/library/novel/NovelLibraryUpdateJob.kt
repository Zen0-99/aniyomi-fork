package eu.kanade.tachiyomi.data.library.novel

import android.content.Context
import androidx.work.WorkerParameters
import tachiyomi.core.common.util.system.logcat

class NovelLibraryUpdateJob(
    context: Context,
    workerParams: WorkerParameters,
) : androidx.work.CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        logcat { "NovelLibraryUpdateJob started" }
        return Result.success()
    }

    companion object {
        private const val TAG = "NovelLibraryUpdateJob"
        private const val WORK_NAME_MANUAL = "NovelLibraryUpdate-$TAG-manual"

        fun startNow(context: Context): Boolean {
            val wm = androidx.work.WorkManager.getInstance(context)
            val workQuery = androidx.work.WorkQuery.Builder
                .fromTags(listOf(TAG))
                .addStates(listOf(androidx.work.WorkInfo.State.RUNNING))
                .build()
            if (wm.getWorkInfos(workQuery).get().isNotEmpty()) {
                return false
            }

            val request = androidx.work.OneTimeWorkRequestBuilder<NovelLibraryUpdateJob>()
                .addTag(TAG)
                .addTag(WORK_NAME_MANUAL)
                .build()
            wm.enqueueUniqueWork(WORK_NAME_MANUAL, androidx.work.ExistingWorkPolicy.KEEP, request)

            return true
        }
    }
}
