package eu.kanade.domain.source.novel.model

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import eu.kanade.tachiyomi.extension.novel.NovelExtensionManager
import tachiyomi.domain.source.novel.model.NovelSource
import tachiyomi.domain.source.novel.model.StubNovelSource
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

val NovelSource.icon: ImageBitmap?
    get() {
        return Injekt.get<NovelExtensionManager>().getAppIconForSource(id)
            ?.toBitmap()
            ?.asImageBitmap()
    }

fun NovelSource.isLocalOrStub(): Boolean = this is StubNovelSource
