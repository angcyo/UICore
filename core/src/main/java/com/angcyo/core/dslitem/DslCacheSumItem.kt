package com.angcyo.core.dslitem

import android.os.StatFs
import com.angcyo.core.R
import com.angcyo.core.component.model.CacheModel
import com.angcyo.core.vmApp
import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.library.ex.dpi
import com.angcyo.library.ex.fileSizeString
import com.angcyo.library.ex.toSizeString
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget.progress.DslProgressBar
import com.angcyo.widget.span.span

/**
 * 缓存总大小
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/10/18
 */
class DslCacheSumItem : DslAdapterItem() {

    val cacheModel = vmApp<CacheModel>()

    init {
        itemLayoutId = R.layout.dsl_cache_sum_item
    }

    override fun onItemBind(
        itemHolder: DslViewHolder,
        itemPosition: Int,
        adapterItem: DslAdapterItem,
        payloads: List<Any>
    ) {
        super.onItemBind(itemHolder, itemPosition, adapterItem, payloads)

        //
        val sum = cacheModel.cacheSumData.value ?: 0L
        itemHolder.tv(R.id.lib_text_view)?.text = span {
            if (sum >= 0) {
                append("共使用") {
                    fontSize = 20 * dpi
                }
                appendln()
                append(sum.toSizeString())
            } else {
                append("计算中...")
            }
        }

        //SD空间信息
        val statFs = StatFs(
            itemHolder.context.getExternalFilesDir("")?.absolutePath
                ?: itemHolder.context.filesDir.absolutePath
        )
        val usedBytes = statFs.totalBytes - statFs.availableBytes
        val progress = usedBytes * 1f / statFs.totalBytes * 100
        itemHolder.v<DslProgressBar>(R.id.lib_progress_bar)?.setProgress(progress)

        //
        itemHolder.tv(R.id.lib_tip_view)?.text = span {
            append(usedBytes.fileSizeString())
            append("/")
            append(statFs.totalBytes.fileSizeString())
        }
    }

}