package eu.kanade.tachiyomi.data.backup.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import tachiyomi.domain.track.novel.model.NovelTrack

@Serializable
data class BackupNovelTracking(
    // in 1.x some of these values have different types or names
    @ProtoNumber(1) var syncId: Int,
    // LibraryId is not null in 1.x
    @ProtoNumber(2) var libraryId: Long,
    @Deprecated("Use mediaId instead", level = DeprecationLevel.WARNING)
    @ProtoNumber(3)
    var mediaIdInt: Int = 0,
    // trackingUrl is called mediaUrl in 1.x
    @ProtoNumber(4) var trackingUrl: String = "",
    @ProtoNumber(5) var title: String = "",
    // lastChapterRead is called last read, and it has been changed to a float in 1.x
    @ProtoNumber(6) var lastChapterRead: Float = 0F,
    @ProtoNumber(7) var totalChapters: Int = 0,
    @ProtoNumber(8) var score: Float = 0F,
    @ProtoNumber(9) var status: Int = 0,
    // startedReadingDate is called startReadTime in 1.x
    @ProtoNumber(10) var startedReadingDate: Long = 0,
    // finishedReadingDate is called endReadTime in 1.x
    @ProtoNumber(11) var finishedReadingDate: Long = 0,
    @ProtoNumber(12) var private: Boolean = false,
    @ProtoNumber(100) var mediaId: Long = 0,
) {

    @Suppress("DEPRECATION")
    fun getTrackImpl(): NovelTrack {
        return NovelTrack(
            id = -1,
            novelId = -1,
            trackerId = this@BackupNovelTracking.syncId.toLong(),
            remoteId = if (this@BackupNovelTracking.mediaIdInt != 0) {
                this@BackupNovelTracking.mediaIdInt.toLong()
            } else {
                this@BackupNovelTracking.mediaId
            },
            libraryId = this@BackupNovelTracking.libraryId,
            title = this@BackupNovelTracking.title,
            lastChapterRead = this@BackupNovelTracking.lastChapterRead.toDouble(),
            totalChapters = this@BackupNovelTracking.totalChapters.toLong(),
            score = this@BackupNovelTracking.score.toDouble(),
            status = this@BackupNovelTracking.status.toLong(),
            startDate = this@BackupNovelTracking.startedReadingDate,
            finishDate = this@BackupNovelTracking.finishedReadingDate,
            remoteUrl = this@BackupNovelTracking.trackingUrl,
            private = this@BackupNovelTracking.private,
        )
    }
}

val backupNovelTrackMapper = {
        _: Long,
        _: Long,
        syncId: Long,
        mediaId: Long,
        libraryId: Long?,
        title: String,
        lastChapterRead: Double,
        totalChapters: Long,
        status: Long,
        score: Double,
        remoteUrl: String,
        startDate: Long,
        finishDate: Long,
        private: Boolean,
    ->
    BackupNovelTracking(
        syncId = syncId.toInt(),
        mediaId = mediaId,
        // forced not null so its compatible with 1.x backup system
        libraryId = libraryId ?: 0,
        title = title,
        lastChapterRead = lastChapterRead.toFloat(),
        totalChapters = totalChapters.toInt(),
        score = score.toFloat(),
        status = status.toInt(),
        startedReadingDate = startDate,
        finishedReadingDate = finishDate,
        trackingUrl = remoteUrl,
        private = private,
    )
}
