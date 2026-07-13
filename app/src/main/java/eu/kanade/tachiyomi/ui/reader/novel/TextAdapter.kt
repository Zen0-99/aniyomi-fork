package eu.kanade.tachiyomi.ui.reader.novel

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import eu.kanade.tachiyomi.R

class TextAdapter(
    private val getConfig: () -> TextConfig,
    private val onNavigationClick: ((TextItem.LoadDirection) -> Unit)? = null,
    private val onTextSelected: ((String) -> Unit)? = null,
    private val onHighlight: ((selectedText: String, start: Int, end: Int) -> Unit)? = null,
    private val onCopy: ((String) -> Unit)? = null,
    private val onShare: ((String) -> Unit)? = null,
    private val onReadAloud: ((String) -> Unit)? = null,
) : ListAdapter<TextItem, TextAdapter.TextViewHolder>(TextItemDiffCallback()) {

    private var currentChapterId: Long = -1L

    fun setCurrentChapterId(id: Long) {
        currentChapterId = id
    }

    fun getCurrentChapterId(): Long = currentChapterId

    abstract class TextViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        abstract fun bind(item: TextItem)
    }

    class ParagraphViewHolder(
        view: View,
        private val getConfig: () -> TextConfig,
        private val onTextSelected: ((String) -> Unit)?,
        private val onHighlight: ((selectedText: String, start: Int, end: Int) -> Unit)?,
        private val onCopy: ((String) -> Unit)?,
        private val onShare: ((String) -> Unit)?,
        private val onReadAloud: ((String) -> Unit)?,
    ) : TextViewHolder(view) {
        private val textView: TextView = view.findViewById(R.id.paragraph_text)

        override fun bind(item: TextItem) {
            if (item is TextItem.Paragraph) {
                val config = getConfig()
                textView.text = item.text
                textView.textSize = config.textSize
                textView.setTextColor(config.textColor)
                textView.setLineSpacing(config.lineSpacing, 1f)
                textView.setPadding(
                    config.horizontalPadding,
                    config.verticalPadding / 2,
                    config.horizontalPadding,
                    config.verticalPadding / 2,
                )
                textView.setTextIsSelectable(config.isTextSelectable)

                if (config.isTextSelectable) {
                    textView.customSelectionActionModeCallback = object : android.view.ActionMode.Callback {
                        override fun onCreateActionMode(mode: android.view.ActionMode, menu: android.view.Menu): Boolean {
                            menu.add(0, MENU_ID_COPY, 0, "Copy")
                            menu.add(0, MENU_ID_DICTIONARY, 1, "Dictionary")
                            menu.add(0, MENU_ID_HIGHLIGHT, 2, "Highlight")
                            menu.add(0, MENU_ID_SHARE, 3, "Share")
                            menu.add(0, MENU_ID_TTS, 4, "Read Aloud")
                            return true
                        }

                        override fun onPrepareActionMode(mode: android.view.ActionMode, menu: android.view.Menu): Boolean = false

                        override fun onActionItemClicked(mode: android.view.ActionMode, item: android.view.MenuItem): Boolean {
                            val selStart = textView.selectionStart
                            val selEnd = textView.selectionEnd
                            if (selStart >= 0 && selEnd > selStart) {
                                val selectedText = textView.text.substring(selStart, selEnd).toString()
                                if (selectedText.isNotBlank()) {
                                    when (item.itemId) {
                                        MENU_ID_COPY -> {
                                            onCopy?.invoke(selectedText)
                                            mode.finish()
                                            return true
                                        }
                                        MENU_ID_DICTIONARY -> {
                                            onTextSelected?.invoke(selectedText)
                                            mode.finish()
                                            return true
                                        }
                                        MENU_ID_HIGHLIGHT -> {
                                            onHighlight?.invoke(selectedText, selStart, selEnd)
                                            mode.finish()
                                            return true
                                        }
                                        MENU_ID_SHARE -> {
                                            onShare?.invoke(selectedText)
                                            mode.finish()
                                            return true
                                        }
                                        MENU_ID_TTS -> {
                                            onReadAloud?.invoke(selectedText)
                                            mode.finish()
                                            return true
                                        }
                                    }
                                }
                            }
                            return false
                        }

                        override fun onDestroyActionMode(mode: android.view.ActionMode) {}
                    }
                }

                when (config.textAlignment) {
                    TextAlignment.LEFT -> textView.gravity = android.view.Gravity.START
                    TextAlignment.CENTER -> textView.gravity = android.view.Gravity.CENTER
                    TextAlignment.JUSTIFY -> {
                        textView.gravity = android.view.Gravity.START
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                            textView.justificationMode = 1
                        }
                    }
                    TextAlignment.RIGHT -> textView.gravity = android.view.Gravity.END
                }

                config.textFont?.let { textView.typeface = it }
            }
        }

        companion object {
            const val MENU_ID_COPY = 100
            const val MENU_ID_DICTIONARY = 101
            const val MENU_ID_HIGHLIGHT = 102
            const val MENU_ID_SHARE = 103
            const val MENU_ID_TTS = 104
        }
    }

    class ChapterHeaderViewHolder(
        view: View,
        private val getConfig: () -> TextConfig,
    ) : TextViewHolder(view) {
        private val titleView: TextView = view.findViewById(R.id.chapter_title)

        override fun bind(item: TextItem) {
            if (item is TextItem.ChapterHeader) {
                val config = getConfig()
                titleView.text = item.chapterTitle
                titleView.setTextColor(config.textColor)
            }
        }
    }

    class LoadingViewHolder(view: View) : TextViewHolder(view) {
        override fun bind(item: TextItem) {}
    }

    class ChapterNavigationViewHolder(
        view: View,
        private val onNavigationClick: ((TextItem.LoadDirection) -> Unit)?,
    ) : TextViewHolder(view) {
        private val button: com.google.android.material.button.MaterialButton =
            view.findViewById(R.id.navigation_button)

        override fun bind(item: TextItem) {
            if (item is TextItem.ChapterNavigation) {
                val buttonText = when {
                    !item.isEnabled && item.direction == TextItem.LoadDirection.NEXT -> "No more chapters"
                    item.direction == TextItem.LoadDirection.PREVIOUS -> "Previous Chapter"
                    item.direction == TextItem.LoadDirection.NEXT -> "Next Chapter"
                    else -> ""
                }
                button.text = buttonText
                button.isEnabled = item.isEnabled
                button.alpha = if (item.isEnabled) 1.0f else 0.5f

                if (item.isEnabled) {
                    button.setOnClickListener { onNavigationClick?.invoke(item.direction) }
                } else {
                    button.setOnClickListener(null)
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is TextItem.Paragraph -> VIEW_TYPE_PARAGRAPH
            is TextItem.ChapterHeader -> VIEW_TYPE_CHAPTER_HEADER
            is TextItem.Loading -> VIEW_TYPE_LOADING
            is TextItem.Error -> VIEW_TYPE_ERROR
            is TextItem.ChapterNavigation -> VIEW_TYPE_CHAPTER_NAVIGATION
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TextViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_PARAGRAPH -> ParagraphViewHolder(
                inflater.inflate(R.layout.item_novel_paragraph, parent, false),
                getConfig,
                onTextSelected,
                onHighlight,
                onCopy,
                onShare,
                onReadAloud,
            )
            VIEW_TYPE_CHAPTER_HEADER -> ChapterHeaderViewHolder(
                inflater.inflate(R.layout.item_novel_chapter_header, parent, false),
                getConfig,
            )
            VIEW_TYPE_LOADING -> LoadingViewHolder(
                inflater.inflate(R.layout.item_novel_loading, parent, false),
            )
            VIEW_TYPE_CHAPTER_NAVIGATION -> ChapterNavigationViewHolder(
                inflater.inflate(R.layout.item_novel_chapter_navigation, parent, false),
                onNavigationClick,
            )
            else -> LoadingViewHolder(
                inflater.inflate(R.layout.item_novel_loading, parent, false),
            )
        }
    }

    override fun onBindViewHolder(holder: TextViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        const val VIEW_TYPE_PARAGRAPH = 0
        const val VIEW_TYPE_CHAPTER_HEADER = 1
        const val VIEW_TYPE_LOADING = 2
        const val VIEW_TYPE_ERROR = 3
        const val VIEW_TYPE_CHAPTER_NAVIGATION = 4
    }
}

class TextItemDiffCallback : DiffUtil.ItemCallback<TextItem>() {
    override fun areItemsTheSame(oldItem: TextItem, newItem: TextItem): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: TextItem, newItem: TextItem): Boolean {
        return oldItem == newItem
    }
}
