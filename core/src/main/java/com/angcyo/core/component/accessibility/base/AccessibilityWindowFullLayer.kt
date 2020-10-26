package com.angcyo.core.component.accessibility.base

import android.view.WindowManager
import com.angcyo.core.R
import com.angcyo.ilayer.ILayer
import com.angcyo.ilayer.container.OffsetPosition
import com.angcyo.ilayer.container.WindowContainer
import com.angcyo.library._contentHeight
import com.angcyo.library.app
import com.angcyo.widget.progress.CircleLoadingView


/**
 * 无障碍悬浮窗 任务状态提示
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/10/19
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
object AccessibilityWindowFullLayer : ILayer() {

    val _windowContainer = WindowContainer(app()).apply {
        defaultOffsetPosition = OffsetPosition(offsetX = 0f, offsetY = 0f)
        wmLayoutParams.apply {
            width = -1
            height = _contentHeight //_contentHeight //-1
            flags = wmLayoutParams.flags or
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        }
    }

    init {
        iLayerLayoutId = R.layout.lib_layout_accessibility_full_window
        autoRestorePosition = false
    }

    fun show() {
        renderLayer = {
            //常亮
            itemView.keepScreenOn = true

            val duration = AccessibilityWindow.duration
            val _hideTime = AccessibilityWindow._hideTime
            if (duration > 0) {
                var animDuration = duration
                var fromProgress = 0

                if (_hideTime in 1 until duration) {
                    fromProgress =
                        (_hideTime * 1f / duration * 100).toInt()
                    animDuration =
                        duration - _hideTime
                }
                v<CircleLoadingView>(R.id.progress_bar)?.setProgress(
                    100,
                    fromProgress,
                    animDuration
                )
            } else if (duration == 0L) {
                v<CircleLoadingView>(R.id.progress_bar)?.setProgress(0)
            }

            tv(R.id.text_view)?.apply {
                this.text = AccessibilityWindow.text
                setTextColor(AccessibilityWindow.textColor)
            }

            visible(R.id.summary_text_view, AccessibilityWindow.summary != null)
            tv(R.id.summary_text_view)?.text = "正在执行:${AccessibilityWindow.summary}"

            tv(R.id.top_text_view)?.text = AccessibilityWindow.fullTopText
            tv(R.id.title_text_view)?.text = AccessibilityWindow.fullTitleText

            //打开本机程序
            throttleClickItem {
                AccessibilityWindow.onLayerClickAction?.invoke()
            }

            throttleClick(R.id.stop_button) {
                AccessibilityWindow.onStopAction?.invoke()
            }

            visible(R.id.catch_button, AccessibilityWindow.showCatchButton)
            //捕捉界面信息
            throttleClick(R.id.catch_button) {
                AccessibilityWindow.onCatchAction?.invoke()
            }
        }

        show(_windowContainer)
    }

    fun hide() {
        hide(_windowContainer)
    }

    //仅显示浮窗
    fun _show() {
        if (_rootView != null && _rootView?.parent == null) {
            show(_windowContainer)
        }
    }
}