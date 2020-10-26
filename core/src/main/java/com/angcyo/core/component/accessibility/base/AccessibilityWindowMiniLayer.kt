package com.angcyo.core.component.accessibility.base

import android.graphics.RectF
import android.view.WindowManager
import com.angcyo.core.R
import com.angcyo.ilayer.ILayer
import com.angcyo.ilayer.container.DragRectFConstraint
import com.angcyo.ilayer.container.WindowContainer
import com.angcyo.library._satusBarHeight
import com.angcyo.library._screenHeight
import com.angcyo.library.app
import com.angcyo.library.ex.isDebugType
import com.angcyo.widget.progress.CircleLoadingView


/**
 * 无障碍悬浮窗 任务状态提示
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/07/03
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
object AccessibilityWindowMiniLayer : ILayer() {

    val _windowContainer = WindowContainer(app()).apply {
        wmLayoutParams.flags = wmLayoutParams.flags or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
    }

    init {
        iLayerLayoutId = R.layout.lib_layout_accessibility_window
        enableDrag = true
        showCancelLayer = true
        dragContainer = DragRectFConstraint(
            RectF(
                0f,
                _satusBarHeight * 1f / _screenHeight,
                0f,
                0.0000001f
            )
        )
    }

    /**[text] 需要提示的文本
     * [summary] 描述文本
     * [duration] 转圈时长, 毫秒. -1 保持原来的进度; 0 清空进度; 其他 进度动画时长*/
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

            visible(R.id.catch_button, AccessibilityWindow.showCatchButton)
            visible(R.id.fullscreen_button, AccessibilityWindow.showCatchButton)

            visible(R.id.summary_text_view, AccessibilityWindow.summary != null)
            tv(R.id.summary_text_view)?.text = AccessibilityWindow.summary

            //打开本机程序
            throttleClickItem {
                AccessibilityWindow.onLayerClickAction?.invoke()
            }

            //切换至全屏
            throttleClick(R.id.fullscreen_button) {
                AccessibilityWindow.fullscreenLayer = true
                AccessibilityWindow.show()

                AccessibilityWindow.onStopAction = {
                    AccessibilityWindow.fullscreenLayer = false
                    //AccessibilityWindow.show()
                }
            }

            //捕捉界面信息
            throttleClick(R.id.catch_button) {
                AccessibilityWindow.onCatchAction?.invoke()
            }

            //测试按钮
            visible(R.id.test_button, isDebugType())
            throttleClick(R.id.test_button) {
                //TouchTipLayer.showTouch(0.2f, 0f)

                //测试手势线
                //TouchTipLayer.showTouch(0.9162f, 0.9762f)   //1.
                TouchTipLayer.showTouch(0.9162f, 0.9562f)   //2.
                //TouchTipLayer.showTouch(0.9162f, 0.9362f)   //3.

                //TouchTipLayer.showMove(0.5f, 0.5f, 0.5f, 0.3f)
                //TouchTipLayer.showMove(0.3f, 0.5f, 0.5f, 0.5f)

                //发送键盘
                /*doBack {
                    try {
                        val inst = Instrumentation()
                        inst.sendKeyDownUpSync(KeyEvent.KEYCODE_ENTER)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }*/

                /*//测试手势点击 "touch:0.9192,0.9842"
                    val p1 = PointF(_screenWidth * 0.9192f, _screenHeight * 0.9842f)
                    BaseAccessibilityService.lastService?.gesture?.click(p1.x, p1.y)
                    L.w(p1)*/

                /*//测试url直接打开抖音
                 val list = listOf(
                     "https://v.douyin.com/JBrY5g9/", //很久的直播 [直播已结束]
                     "https://v.douyin.com/JBxoeKL", //直播 [打开看看]
                     "https://v.douyin.com/J6mdAPq/", //视频
                     "https://v.kuaishou.com/8vnjyY" //快手视频
                 )
                 val url = list[0]

                 Intent().apply {
                     setPackage("com.ss.android.ugc.aweme")
                     //setPackage("com.smile.gifmaker")
                     data = Uri.parse(url)
                     baseConfig(it.context)

                     try {
                         it.context.startActivity(this)
                     } catch (e: Exception) {
                         e.printStackTrace()
                     }
                 }*/

                /*val imm = context.getSystemService(
                    Context.INPUT_METHOD_SERVICE
                ) as InputMethodManager

                doBack {
                    LTime.tick()
                    try {
                        val inst = Instrumentation()
                        inst.sendKeyDownUpSync(KeyEvent.KEYCODE_ENTER)
                    } catch (e: Exception) {
                        L.e("Exception when sendKeyDownUpSync $e")
                    }
                    L.i(LTime.time())
                }

                try {
                    val keyCommand = "input keyevent " + KeyEvent.KEYCODE_ENTER
                    val runtime = Runtime.getRuntime()
                    val proc = runtime.exec(keyCommand)
                    //L.i(proc.waitFor(), " ", proc.exitValue())
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                doBack {
                    try {
                        val keyCommand = "input keyevent " + KeyEvent.KEYCODE_ENTER
                        val runtime = Runtime.getRuntime()
                        val proc = runtime.exec(keyCommand)
                        //L.i(proc.waitFor(), " ", proc.exitValue())
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                val inputConnection = BaseInputConnection(it, true)
                L.e(inputConnection.performEditorAction(EditorInfo.IME_ACTION_SEARCH))*/
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