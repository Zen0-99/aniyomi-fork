package eu.kanade.tachiyomi.source.novel

import eu.kanade.tachiyomi.novelsource.NovelSource
import tachiyomi.domain.source.novel.model.StubNovelSource

fun NovelSource.isLocalOrStub(): Boolean = this is StubNovelSource
