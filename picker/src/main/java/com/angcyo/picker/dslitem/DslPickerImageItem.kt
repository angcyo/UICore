package com.angcyo.picker.dslitem

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.TransitionDrawable
import android.view.animation.AccelerateInterpolator
import androidx.annotation.DrawableRes
import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.dsladapter.containsPayload
import com.angcyo.dsladapter.isUpdateMedia
import com.angcyo.dsladapter.margin
import com.angcyo.glide.giv
import com.angcyo.library.ex.*
import com.angcyo.library.ex.mimeType
import com.angcyo.library.model.*
import com.angcyo.picker.R
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget.base.bgColorAnimator
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

    companion object {
        //动画更新
        const val PAYLOAD_UPDATE_ANIM = PAYLOAD_UPDATE_PART + 1

        //无法选中的动画更新
        const val PAYLOAD_UPDATE_CANCEL_ANIM = PAYLOAD_UPDATE_PART + 2
    }

    /**要加载的媒体*/
    var loaderMedia: LoaderMedia? = null
        get() = field ?: (itemData as? LoaderMedia)

    /**选中的索引数值*/
    var onGetSelectedIndex: (LoaderMedia?) -> CharSequence? = { null }

    /**是否选中*/
    var onGetSelectedState: (LoaderMedia?) -> Boolean = { itemIsSelected }

    /**请求选择item*/
    var onSelectorItem: (selected: Boolean) -> Unit = {}

    /**选择模式*/
    var checkModel = true

    /**显示文件大小*/
    var showFileSize: Boolean = false

    /**占位图*/
    var itemPlaceholderDrawable: Drawable? = colorDrawable(R.color.picker_image_placeholder_color)

    @DrawableRes
    var itemAudioCoverTipDrawable: Int = com.angcyo.item.R.drawable.lib_audio_cover_tip

    @DrawableRes
    var itemVideoCoverTipDrawable: Int = -1

    @DrawableRes
    var itemAudioTipDrawable: Int = com.angcyo.item.R.drawable.lib_audio_tip

    @DrawableRes
    var itemVideoTipDrawable: Int = com.angcyo.item.R.drawable.lib_video_tip

    @DrawableRes
    var itemImageTipDrawable: Int = com.angcyo.item.R.drawable.lib_image_tip

    var _transitionDuration = 160
    var _transitionInterpolator = AccelerateInterpolator()

    var _selectorMaskColor = Color.parseColor("#AA000000")

    init {
        itemLayoutId = R.layout.dsl_picker_image_layout

        thisAreContentsTheSame = { _, newItem ->
            when {
                itemChanging -> false
                else -> (newItem as? DslPickerImageItem)?.loaderMedia == this.loaderMedia
            }
        }

        margin(1)

        //长按赋值媒体信息
        itemLongClick = {
            it.longFeedback()
            loaderMedia?.toString()?.copy(it.context)
            true
        }

        //大图预览
        itemClick = {

        }
    }

    override fun onItemBind(
        itemHolder: DslViewHolder,
        itemPosition: Int,
        adapterItem: DslAdapterItem,
        payloads: List<Any>
    ) {
        super.onItemBind(itemHolder, itemPosition, adapterItem, payloads)

        //更新媒体
        val mediaUpdate = payloads.isUpdateMedia()
        val animUpdate = payloads.containsPayload(PAYLOAD_UPDATE_ANIM)
        val cancelAnimUpdate = payloads.containsPayload(PAYLOAD_UPDATE_CANCEL_ANIM)

        if (mediaUpdate) {
            //缩略图
            itemHolder.giv(R.id.lib_image_view)?.apply {
                setImageDrawable(itemPlaceholderDrawable)
                if (!loaderMedia.isAudio()) {
                    load(loaderMedia?.loadUri()) {
                        placeholderDrawable = itemPlaceholderDrawable
                        checkGifType = true
                    }
                }
            }
        }

        //model
        itemHolder.visible(R.id.index_layout, checkModel)

        //audio video tip
        itemHolder.gone(R.id.lib_tip_image_view)
        if (loaderMedia?.isAudio() == true) {
            if (itemAudioCoverTipDrawable > 0) {
                itemHolder.img(R.id.lib_tip_image_view)?.setImageResource(itemAudioCoverTipDrawable)
                itemHolder.visible(R.id.lib_tip_image_view)
            }
        } else if (loaderMedia?.isVideo() == true) {
            if (itemVideoCoverTipDrawable > 0) {
                itemHolder.img(R.id.lib_tip_image_view)?.setImageResource(itemVideoCoverTipDrawable)
                itemHolder.visible(R.id.lib_tip_image_view)
            }
        }

        //文本
        if (loaderMedia?.isVideo() == true ||
            loaderMedia?.isAudio() == true ||
            !(loaderMedia?.cropPath.isNullOrBlank())/*如果图片被剪裁过*/) {
            itemHolder.visible(R.id.duration_view)

            //时长
            itemHolder.tv(R.id.duration_view)?.text = span {
                drawable {
                    backgroundDrawable =
                        if (loaderMedia?.isVideo() == true && itemVideoTipDrawable > 0) {
                            _drawable(itemVideoTipDrawable)
                        } else if (loaderMedia?.isAudio() == true && itemAudioTipDrawable > 0) {
                            _drawable(itemAudioTipDrawable)
                        } else if (!(loaderMedia?.cropPath.isNullOrBlank()) && itemImageTipDrawable > 0) {
                            _drawable(itemImageTipDrawable)
                        } else {
                            null
                        }
                }
                val _duration = loaderMedia?.duration ?: 0L
                if (_duration > 0) {
                    appendSpace(6 * dpi)
                    //不足1秒的取1秒
                    val duration = if (_duration != 0L) max(_duration, 1_000) else 0
                    append(
                        duration.toElapsedTime(
                            pattern = intArrayOf(-1, 1, 1),
                            h24 = booleanArrayOf(false, true, false),
                            units = arrayOf("", "", ":")
                        )
                    )
                }
            }
        } else {
            itemHolder.gone(R.id.duration_view)
        }

        //事件
        itemHolder.click(R.id.index_layout) {
            onSelectorItem(onGetSelectedState(loaderMedia))
        }
        itemHolder.giv(R.id.lib_image_view)?.apply {
            setOnClickListener(_clickListener)
            setOnLongClickListener(_longClickListener)
        }

        //debug
        when {
            isDebug() -> {
                itemHolder.visible(R.id.lib_tip_text_view)
                itemHolder.tv(R.id.lib_tip_text_view)?.text = span {
                    append("${loaderMedia?.width ?: 0}")
                    append("x")
                    append("${loaderMedia?.height ?: 0}")
                    append(" ${loaderMedia?.duration}")
                    if (loaderMedia?.fileSize ?: -1 > 0) {
                        appendln()
                        append(loaderMedia?.fileSize?.fileSizeString() ?: "")
                    }
                    appendln()
                    append(loaderMedia?.mimeType() ?: "")
                }
            }
            showFileSize -> {
                itemHolder.visible(R.id.lib_tip_text_view)
                itemHolder.tv(R.id.lib_tip_text_view)?.text = span {
                    append(loaderMedia?.fileSize?.fileSizeString() ?: "")
                }
            }
            else -> {
                itemHolder.gone(R.id.lib_tip_text_view)
            }
        }

        if (animUpdate) {
            //点击时的动画, 在负载更新中执行
            if (onGetSelectedState(loaderMedia)) {
                itemHolder.tv(R.id.index_view)?.background = TransitionDrawable(
                    arrayOf(
                        _drawable(R.drawable.ic_picker_check_normal),
                        _drawable(R.drawable.picker_index_checked_shape)
                    )
                ).apply {
                    startTransition(_transitionDuration)
                }

                itemHolder.view(R.id.selector_mask_view)
                    ?.bgColorAnimator(
                        Color.TRANSPARENT,
                        _selectorMaskColor,
                        _transitionInterpolator,
                        _transitionDuration.toLong()
                    )
            } else {
                itemHolder.tv(R.id.index_view)
                    ?.setBackgroundResource(R.drawable.ic_picker_check_normal)

                itemHolder.view(R.id.selector_mask_view)
                    ?.bgColorAnimator(
                        _selectorMaskColor,
                        Color.TRANSPARENT,
                        _transitionInterpolator,
                        _transitionDuration.toLong()
                    )
            }
        } else if (cancelAnimUpdate) {
            //无法选中的动画
            itemHolder.tv(R.id.index_view)?.run {
                val duration = _transitionDuration
                background = TransitionDrawable(
                    arrayOf(
                        _drawable(R.drawable.picker_index_checked_shape),
                        _drawable(R.drawable.lib_white_circle_shape)
                    )
                ).apply {
                    startTransition(duration)
                }
                postDelayed({
                    setBackgroundResource(R.drawable.ic_picker_check_normal)
                }, duration.toLong())
            }
        } else {
            //索引背景
            itemHolder.view(R.id.index_view)?.setBackgroundResource(
                if (onGetSelectedState(loaderMedia)) {
                    R.drawable.picker_index_checked_shape
                } else {
                    R.drawable.ic_picker_check_normal
                }
            )
            //选中遮罩, 选中提示背景
            itemHolder.view(R.id.selector_mask_view)?.setBackgroundColor(
                if (onGetSelectedState(loaderMedia)) {
                    _selectorMaskColor
                } else {
                    Color.TRANSPARENT
                }
            )
        }

        //索引
        itemHolder.tv(R.id.index_view)?.apply {
            text = onGetSelectedIndex(loaderMedia)
        }
    }
}