package com.angcyo.core.dslitem

import com.angcyo.core.R
import com.angcyo.core.component.model.CacheInfo
import com.angcyo.core.component.model.CacheModel
import com.angcyo.core.vmApp
import com.angcyo.dialog.messageDialog
import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.library.ex.toSizeString
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget.span.span

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/10/17
 */
class DslCacheModelItem : DslAdapterItem() {

    var itemCacheInfo: CacheInfo? = null

    init {
        itemLayoutId = R.layout.dsl_cache_mode_item
    }

    override fun onItemBind(
        itemHolder: DslViewHolder,
        itemPosition: Int,
        adapterItem: DslAdapterItem,
        payloads: List<Any>
    ) {
        super.onItemBind(itemHolder, itemPosition, adapterItem, payloads)

        itemHolder.tv(R.id.lib_label_view)?.text = itemCacheInfo?.label
        itemHolder.tv(R.id.lib_des_view)?.text = span {
            itemCacheInfo?.des?.let {
                append(it)
                appendln()
            }
            itemCacheInfo?.path?.let {
                append(it)
            }
        }
        val size = itemCacheInfo?._size ?: -1
        itemHolder.tv(R.id.lib_size_view)?.text = if (size >= 0) {
            size.toSizeString()
        } else {
            "..."
        }

        itemHolder.visible(R.id.lib_clear_view, size > 0)

        //
        itemHolder.click(R.id.lib_clear_view) { view ->
            itemCacheInfo?.let { cacheInfo ->
                view.context.messageDialog {
                    dialogTitle = "清理提示"
                    dialogMessage = "文件清除之后,无法恢复,是否继续?"
                    positiveButton { dialog, dialogViewHolder ->
                        dialog.dismiss()
                        vmApp<CacheModel>().clearCache(cacheInfo)
                    }
                    negativeButton { dialog, dialogViewHolder ->
                        dialog.dismiss()
                    }
                }
            }
        }
    }

}