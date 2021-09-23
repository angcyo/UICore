package com.angcyo.item

import android.view.View
import androidx.annotation.DrawableRes
import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.glide.R
import com.angcyo.item.style.IImageItem
import com.angcyo.item.style.ImageItemConfig
import com.angcyo.library.ex.*
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget.span.span
import kotlin.math.max

/**
 * 简单的图片展示item
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/02/19
 */

open class DslImageItem : DslAdapterItem(), IImageItem {


    /**媒体类型, 为空会在[itemLoadUri]解析*/
    open var itemMimeType: String? = null

    /**视频或者音频时长, 毫秒*/
    open var itemMediaDuration: Long = -1

    /**显示cover*/
    var itemShowCover: Boolean = true

    @DrawableRes
    var itemAudioCoverTipDrawable: Int = R.drawable.lib_audio_cover_tip

    @DrawableRes
    var itemVideoCoverTipDrawable: Int = R.drawable.lib_video_cover_tip

    /**显示Tip*/
    var itemShowTip: Boolean = true

    @DrawableRes
    var itemAudioTipDrawable: Int = R.drawable.lib_audio_tip

    @DrawableRes
    var itemVideoTipDrawable: Int = R.drawable.lib_video_tip

    /**是否显示删除按钮*/
    var itemShowDeleteView: Boolean = false

    var itemDeleteClick: (View) -> Unit = {}

    override var imageItemConfig: ImageItemConfig = ImageItemConfig()

    init {
        itemLayoutId = R.layout.dsl_image_item
    }

    override fun onItemBind(
        itemHolder: DslViewHolder,
        itemPosition: Int,
        adapterItem: DslAdapterItem,
        payloads: List<Any>
    ) {
        super.onItemBind(itemHolder, itemPosition, adapterItem, payloads)
        //更新媒体
        //initImageItem(itemHolder, payloads)

        //audio video tip
        itemHolder.gone(R.id.lib_tip_image_view)
        itemHolder.gone(R.id.lib_duration_view)

        //mimeType
        val mimeType = itemMimeType ?: imageItemConfig.itemLoadUri?.loadUrl()?.mimeType()

        val isVideo = mimeType?.isVideoMimeType() == true
        val isAudio = mimeType?.isAudioMimeType() == true
        if (itemShowCover) {
            if (isAudio) {
                if (itemAudioCoverTipDrawable > 0) {
                    itemHolder.visible(R.id.lib_tip_image_view)
                    itemHolder.img(R.id.lib_tip_image_view)
                        ?.setImageResource(itemAudioCoverTipDrawable)
                }
            } else if (isVideo) {
                if (itemVideoCoverTipDrawable > 0) {
                    itemHolder.visible(R.id.lib_tip_image_view)
                    itemHolder.img(R.id.lib_tip_image_view)
                        ?.setImageResource(itemVideoCoverTipDrawable)
                }
            }
        }

        //时长
        if (itemShowTip) {

            if (isVideo || isAudio) {
                itemHolder.visible(R.id.lib_duration_view)

                itemHolder.tv(R.id.lib_duration_view)?.text = span {
                    drawable {
                        backgroundDrawable =
                            if (isVideo && itemVideoTipDrawable > 0) {
                                _drawable(itemVideoTipDrawable)
                            } else if (isAudio && itemAudioTipDrawable > 0) {
                                _drawable(itemAudioTipDrawable)
                            } else {
                                null
                            }
                    }

                    if (itemMediaDuration > 0) {
                        appendSpace(6 * dpi)
                        val _duration = itemMediaDuration
                        //不足1秒的取1秒
                        val duration = if (_duration != 0L) max(_duration, 1_000) else 0
                        append(
                            duration.toElapsedTime(
                                pattern = intArrayOf(-1, 1, 1),
                                refill = booleanArrayOf(false, true, false),
                                units = arrayOf("", "", ":")
                            )
                        )
                    }
                }
            }
        }

        itemHolder.visible(R.id.lib_delete_view, itemShowDeleteView)
        itemHolder.click(R.id.lib_delete_view, itemDeleteClick)
    }
}