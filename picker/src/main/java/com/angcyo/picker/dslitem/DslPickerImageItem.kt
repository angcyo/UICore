package com.angcyo.picker.dslitem

import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.dsladapter.margin
import com.angcyo.glide.giv
import com.angcyo.library.ex.*
import com.angcyo.loader.LoaderMedia
import com.angcyo.loader.isAudio
import com.angcyo.loader.isVideo
import com.angcyo.loader.loadUri
import com.angcyo.picker.R
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget.base.longFeedback
import com.angcyo.widget.span.span
import kotlin.math.max

/**
 * 图片选择器, 小图片选择布局
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/02/01
 */

open class DslPickerImageItem : DslAdapterItem() {

    val loaderMedia: LoaderMedia? get() = itemData as? LoaderMedia

    init {
        itemLayoutId = R.layout.dsl_picker_image_layout

        thisAreContentsTheSame = { _, newItem ->
            when {
                itemChanging -> false
                else -> (newItem as? DslPickerImageItem)?.loaderMedia == this.loaderMedia
            }
        }
        margin(1 * dpi)

        //长按赋值媒体信息
        onItemLongClick = {
            it.longFeedback()
            loaderMedia?.toString()?.copy(it.context)
            true
        }

        //大图预览
        onItemClick = {

        }
    }

    override fun onItemBind(
        itemHolder: DslViewHolder,
        itemPosition: Int,
        adapterItem: DslAdapterItem
    ) {
        super.onItemBind(itemHolder, itemPosition, adapterItem)

        //缩略图
        itemHolder.giv(R.id.lib_image_view)?.apply {
            drawBorder = itemIsSelected
            load(loaderMedia?.loadUri()) {
                checkGifType = true
            }
            setOnClickListener(_clickListener)
            setOnLongClickListener(_longClickListener)
        }

        //audio
        itemHolder.visible(R.id.lib_tip_image_view, loaderMedia?.isAudio() == true)

        //文本
        if (loaderMedia?.isVideo() == true || loaderMedia?.isAudio() == true) {
            itemHolder.visible(R.id.duration_view)

            //时长
            itemHolder.tv(R.id.duration_view)?.text = span {
                drawable {
                    backgroundDrawable = if (loaderMedia?.isVideo() == true) {
                        offsetY = 2 * dp
                        getDrawable(R.drawable.ic_picker_video)
                    } else {
                        getDrawable(R.drawable.ic_picker_audio)
                    }
                }
                appendSpace(6 * dpi)
                //不足1秒的取1秒
                val duration = max(loaderMedia!!.duration, 1_000)
                append(
                    duration.toElapsedTime(
                        pattern = intArrayOf(-1, 1, 1),
                        h24 = booleanArrayOf(false, true, false),
                        units = arrayOf("", "", ":")
                    )
                )
            }
        } else {
            itemHolder.gone(R.id.duration_view)
        }

        //索引
    }
}