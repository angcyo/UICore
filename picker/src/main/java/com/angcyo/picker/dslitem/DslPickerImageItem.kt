package com.angcyo.picker.dslitem

import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.dsladapter.margin
import com.angcyo.glide.giv
import com.angcyo.library.ex.dp
import com.angcyo.library.ex.dpi
import com.angcyo.library.ex.getDrawable
import com.angcyo.library.ex.toElapsedTime
import com.angcyo.loader.LoaderMedia
import com.angcyo.loader.isAudio
import com.angcyo.loader.isVideo
import com.angcyo.loader.loadPath
import com.angcyo.picker.R
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget.base.clickIt
import com.angcyo.widget.span.span

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/02/01
 */

open class DslPickerImageItem : DslAdapterItem() {
    init {
        itemLayoutId = R.layout.dsl_picker_image_layout

        thisAreContentsTheSame = { _, newItem ->
            when {
                itemChanging -> false
                else -> (newItem as? DslPickerImageItem)?.loaderMedia == this.loaderMedia
            }
        }
        margin(1 * dpi)
    }

    var loaderMedia: LoaderMedia? = null

    override fun onItemBind(
        itemHolder: DslViewHolder,
        itemPosition: Int,
        adapterItem: DslAdapterItem
    ) {
        super.onItemBind(itemHolder, itemPosition, adapterItem)

        //缩略图
        itemHolder.giv(R.id.lib_image_view)?.apply {
            drawBorder = itemIsSelected
            load(loaderMedia?.loadPath()) {
                checkGifType = true
            }

            clickIt {
                //大图浏览
            }
        }

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
                val duration = loaderMedia!!.duration
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