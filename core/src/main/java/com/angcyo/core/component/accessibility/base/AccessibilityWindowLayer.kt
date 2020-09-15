package com.angcyo.core.component.accessibility.base

import android.content.Intent
import android.graphics.Color
import android.graphics.RectF
import android.net.Uri
import com.angcyo.core.R
import com.angcyo.core.component.accessibility.AccessibilityHelper
import com.angcyo.core.component.accessibility.LogWindowAccessibilityInterceptor
import com.angcyo.core.component.file.DslFileHelper
import com.angcyo.core.component.file.wrapData
import com.angcyo.http.rx.doBack
import com.angcyo.ilayer.ILayer
import com.angcyo.ilayer.container.DragRectFConstraint
import com.angcyo.ilayer.container.WindowContainer
import com.angcyo.library._satusBarHeight
import com.angcyo.library._screenHeight
import com.angcyo.library.app
import com.angcyo.library.component.MainExecutor
import com.angcyo.library.ex.*
import com.angcyo.widget.progress.CircleLoadingView


/**
 * 无障碍悬浮窗 任务状态提示
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/07/03
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
object AccessibilityWindowLayer : ILayer() {

    /**触发的保存窗口日志*/
    var onSaveWindowLog: ((log: String) -> Unit)? = null

    val _windowContainer = WindowContainer(app())

    /**浮窗需要隐藏到什么时间, 13位时间戳*/
    var _hideToTime: Long = -1

    //隐藏时长
    var _hideTime: Long = -1

    /**还需要隐藏的次数, >0生效*/
    var _hideToCount: Long = -1

    //显示浮窗
    val _showRunnable: Runnable? = Runnable {
        _show()
    }

    val _defaultClickAction: Action = {
        app().openApp()
    }

    /**点击浮窗的回调*/
    var onLayerClickAction: Action? = _defaultClickAction

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
    fun show(
        text: CharSequence?,
        summary: CharSequence? = null,
        duration: Long = -1,
        textColor: Int = Color.WHITE
    ) {

        var hide = false

        if (_hideToCount > 0) {
            //仍然需要隐藏浮窗
            hide = true
        }

        val nowTime: Long = nowTime()

        if (nowTime <= _hideToTime) {
            //仍然需要隐藏浮窗
            hide = true
        }

        renderLayer = {
            //常亮
            itemView.keepScreenOn = true

            if (duration > 0) {
                var animDuration = duration
                var fromProgress = 0

                if (_hideTime in 1 until duration) {
                    fromProgress = (_hideTime * 1f / duration * 100).toInt()
                    animDuration = duration - _hideTime
                }
                v<CircleLoadingView>(R.id.progress_bar)?.setProgress(
                    100,
                    fromProgress,
                    animDuration
                )
            } else if (duration == 0L) {
                v<CircleLoadingView>(R.id.progress_bar)?.setProgress(0)
            }

            //clear
            _hideTime = 0

            tv(R.id.text_view)?.apply {
                this.text = text
                setTextColor(textColor)
            }

            visible(R.id.catch_button, isDebug())
            visible(R.id.summary_text_view, summary != null)
            tv(R.id.summary_text_view)?.text = summary

            //打开本机程序
            throttleClickItem {
                onLayerClickAction?.invoke()
            }

            //捕捉界面信息
            throttleClick(R.id.catch_button) {
                doBack {
                    val logWindow =
                        LogWindowAccessibilityInterceptor.logWindow(showToast = true)
                    if (!logWindow.isNullOrEmpty()) {

                        val log = logWindow.wrapData()
                        DslFileHelper.write(
                            AccessibilityHelper.logFolderName,
                            "catch.log",
                            log
                        )

                        onSaveWindowLog?.invoke(log)
                    }
                }
            }

            //测试按钮
            visible(R.id.test_button, isDebugType())
            throttleClick(R.id.test_button) {

                //测试手势点击 "touch:0.9192,0.9842"
//                val p1 = PointF(_screenWidth * 0.9192f, _screenHeight * 0.9842f)
//                BaseAccessibilityService.lastService?.gesture?.click(p1.x, p1.y)
//                L.w(p1)

                //测试url直接打开抖音
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
                }

//                val imm = context.getSystemService(
//                    Context.INPUT_METHOD_SERVICE
//                ) as InputMethodManager

//                doBack {
//                    LTime.tick()
//                    try {
//                        val inst = Instrumentation()
//                        inst.sendKeyDownUpSync(KeyEvent.KEYCODE_ENTER)
//                    } catch (e: Exception) {
//                        L.e("Exception when sendKeyDownUpSync $e")
//                    }
//                    L.i(LTime.time())
//                }

//                try {
//                    val keyCommand = "input keyevent " + KeyEvent.KEYCODE_ENTER
//                    val runtime = Runtime.getRuntime()
//                    val proc = runtime.exec(keyCommand)
//                    //L.i(proc.waitFor(), " ", proc.exitValue())
//                } catch (e: Exception) {
//                    e.printStackTrace()
//                }

//                doBack {
//                    try {
//                        val keyCommand = "input keyevent " + KeyEvent.KEYCODE_ENTER
//                        val runtime = Runtime.getRuntime()
//                        val proc = runtime.exec(keyCommand)
//                        //L.i(proc.waitFor(), " ", proc.exitValue())
//                    } catch (e: Exception) {
//                        e.printStackTrace()
//                    }
//                }

//                val inputConnection = BaseInputConnection(it, true)
//                L.e(inputConnection.performEditorAction(EditorInfo.IME_ACTION_SEARCH))
            }
        }

        if (hide) {
            hide()
        } else {
            show(_windowContainer)
        }
    }

    fun hide() {
        hide(_windowContainer)
    }

    /**异常多少次的显示请求*/
    fun hideCount(count: Long) {
        hideTime(-1)
        _hideToCount = count
    }

    /**浮窗隐藏多长时间*/
    fun hideTime(time: Long) {
        _hideToCount = -1

        _showRunnable?.let {
            MainExecutor.handler.removeCallbacks(it)
        }

        if (time > 0) {
            _hideTime = time
            _hideToTime = time + nowTime()

            _showRunnable?.let {
                MainExecutor.handler.postDelayed(it, time)
            }
        } else {
            _hideTime = -1
            _hideToTime = -1
        }
    }

    //仅显示浮窗
    fun _show() {
        if (_rootView != null && _rootView?.parent == null) {
            show(_windowContainer)
        }
    }
}