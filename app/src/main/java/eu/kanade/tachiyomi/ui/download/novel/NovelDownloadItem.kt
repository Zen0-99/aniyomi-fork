package eu.kanade.tachiyomi.ui.download.novel

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.items.AbstractSectionableItem
import eu.davidea.flexibleadapter.items.IFlexible
import eu.kanade.tachiyomi.R
import eu.kanade.tachiyomi.data.download.novel.model.NovelDownload

class NovelDownloadItem(
    val download: NovelDownload,
    header: NovelDownloadHeaderItem,
) : AbstractSectionableItem<NovelDownloadHolder, NovelDownloadHeaderItem>(header) {

    override fun getLayoutRes(): Int {
        return R.layout.download_item
    }

    override fun createViewHolder(
        view: View,
        adapter: FlexibleAdapter<IFlexible<RecyclerView.ViewHolder>>,
    ): NovelDownloadHolder {
        return NovelDownloadHolder(view, adapter as NovelDownloadAdapter)
    }

    override fun bindViewHolder(
        adapter: FlexibleAdapter<IFlexible<RecyclerView.ViewHolder>>,
        holder: NovelDownloadHolder,
        position: Int,
        payloads: MutableList<Any>,
    ) {
        holder.bind(download)
    }

    override fun isDraggable(): Boolean {
        return true
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other is NovelDownloadItem) {
            return download.chapter.id == other.download.chapter.id
        }
        return false
    }

    override fun hashCode(): Int {
        return download.chapter.id.toInt()
    }
}
