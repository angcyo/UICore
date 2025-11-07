package com.angcyo.item

import android.view.MotionEvent
import android.view.View
import com.angcyo.dialog.TargetWindow
import com.angcyo.dialog.popup.PopupTipConfig
import com.angcyo.dialog.popup.popupTipWindow
import com.angcyo.drawable.BubbleDrawable
import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.dsladapter.annotation.UpdateByDiff
import com.angcyo.dsladapter.annotation.UpdateByNotify
import com.angcyo.library._screenWidth
import com.angcyo.library.ex.interceptParentTouchEvent
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget.progress.DslProgressBar
import com.angcyo.widget.progress.DslSeekBar

/**
 * [com.angcyo.widget.progress.DslSeekBar] 滑动
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/05/16
 */
open class DslSeekBarInfoItem : DslBaseInfoItem() {

    /**[min-max] 非比例值*/
    var itemSeekProgress: Float = 0f

    /**是否显示进度文本*/
    var itemShowProgressText: Boolean = false

    /**是否激活popup提示
     * [PopupTipConfig]*/
    var itemEnableSeekPopupTip: Boolean = true

    /**提示布局id*/
    var itemPopupTipLayoutId: Int = R.layout.lib_bubble_tip_layout

    /**进度文本格式化*/
    var itemProgressTextFormatAction: (DslProgressBar) -> String = {
        it.progressTextFormat.format("${(it._progressFraction * 100).toInt()}")
    }

    /**进度改变回调,
     * [value] 进度值[min]~[max]
     * [fraction] 进度比例
     * [fromUser] 是否是用户触发*/
    var itemSeekChanged: (value: Float, fraction: Float, fromUser: Boolean) -> Unit = { _, _, _ -> }

    /**Touch结束后的回调*/
    var itemSeekTouchEnd: (value: Float, fraction: Float) -> Unit = { _, _ -> }

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
        initSeekBarView(itemHolder, itemPosition, adapterItem, payloads)
    }

    /**初始化滑块*/
    open fun initSeekBarView(
        itemHolder: DslViewHolder,
        itemPosition: Int,
        adapterItem: DslAdapterItem,
        payloads: List<Any>
    ) {
        itemHolder.v<DslSeekBar>(R.id.lib_seek_view)?.apply {
            showProgressText = itemShowProgressText
            progressTextFormatAction = itemProgressTextFormatAction

            setProgress(itemSeekProgress, progressValue, -1)
            config {
                onSeekChanged = { value, fraction, fromUser ->
                    this@DslSeekBarInfoItem.onItemSeekChanged(value, fraction, fromUser)
                }
                onSeekTouchEnd = itemSeekTouchEnd
            }
        }
        if (itemEnableSeekPopupTip) {
            itemHolder.touch(R.id.lib_seek_view) { view, event ->
                showBubblePopupTip(view, event)
                true
            }
        }
    }

    /**滑块改变后触发
     * [value] 进度值 [min~max]
     * [fraction] 比例值 [0~1f]*/
    @UpdateByDiff
    @UpdateByNotify
    open fun onItemSeekChanged(value: Float, fraction: Float, fromUser: Boolean) {
        if (fromUser) {
            itemChanging = true
        }
        itemSeekChanged(value, fraction, fromUser)
    }

    //---弹窗提示---

    var _window: TargetWindow? = null
    var _popupTipConfig: PopupTipConfig? = null
    var itemPopupShowInDialogWindows: Boolean = false

    open fun showBubblePopupTip(view: View, event: MotionEvent) {
        view.interceptParentTouchEvent(event)
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                _window = view.context.popupTipWindow(view, itemPopupTipLayoutId) {
                    touchX = event.x
                    _popupTipConfig = this
                    isShowInDialogWindows = itemPopupShowInDialogWindows
                    onInitLayout = { window, viewHolder ->
                        viewHolder.view(R.id.lib_bubble_view)?.background = BubbleDrawable()
                        viewHolder.tv(R.id.lib_text_view)?.text = if (view is DslProgressBar) {
                            itemProgressTextFormatAction(view)
                        } else {
                            "${(touchX * 1f / _screenWidth * 100).toInt()}"
                        }
                    }
                    if (view is DslSeekBar) {
                        limitTouchRect = view._progressBound
                    }
                }
            }

            MotionEvent.ACTION_MOVE -> {
                _popupTipConfig?.apply {
                    touchX = event.x
                    updatePopup()
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                //window?.dismiss()
                _popupTipConfig?.hide()
            }
        }
    }
}