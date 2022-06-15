package com.angcyo.item

import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget.progress.DslProgressBar
import com.angcyo.widget.progress.DslSeekBar

/**
 * [com.angcyo.widget.progress.DslSeekBar] 滑动
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/05/16
 */
open class DslSeekBarInfoItem : DslBaseInfoItem() {

    /**[0-100]*/
    var itemSeekProgress: Int = 0

    /**是否显示进度文本*/
    var itemShowProgressText: Boolean = false

    /**进度文本格式化*/
    var itemProgressTextFormatAction: (DslProgressBar) -> String = {
        it.progressTextFormat.format("${(it._progressFraction * 100).toInt()}")
    }

    /**进度改变回调,
     * [value] 进度值
     * [fraction] 进度比例
     * [fromUser] 是否是用户触发*/
    var itemSeekChanged: (value: Int, fraction: Float, fromUser: Boolean) -> Unit = { _, _, _ -> }

    /**Touch结束后的回调*/
    var itemSeekTouchEnd: (value: Int, fraction: Float) -> Unit = { _, _ -> }

    init {
        itemExtendLayoutId = R.layout.dsl_extent_seek_item
    }

    override fun onItemBind(
        itemHolder: DslViewHolder,
        itemPosition: Int,
        adapterItem: DslAdapterItem,
        payloads: List<Any>
    ) {
        super.onItemBind(itemHolder, itemPosition, adapterItem, payloads)

        itemHolder.v<DslSeekBar>(R.id.lib_seek_view)?.apply {
            showProgressText = itemShowProgressText
            progressTextFormatAction = itemProgressTextFormatAction

            setProgress(itemSeekProgress, progressValue, -1)
            config {
                onSeekChanged = { value, fraction, fromUser ->
                    if (fromUser) {
                        itemChanging = true
                    }
                    itemSeekChanged(value, fraction, fromUser)
                }
                onSeekTouchEnd = itemSeekTouchEnd
            }
        }
    }

}