package com.angcyo.core.component.accessibility.base

import android.graphics.PointF
import android.graphics.RectF
import com.angcyo.core.R
import com.angcyo.core.component.accessibility.AccessibilityHelper
import com.angcyo.core.component.accessibility.BaseAccessibilityService
import com.angcyo.core.component.accessibility.LogWindowAccessibilityInterceptor
import com.angcyo.core.component.accessibility.click
import com.angcyo.core.component.file.DslFileHelper
import com.angcyo.core.component.file.wrapData
import com.angcyo.http.rx.doBack
import com.angcyo.ilayer.ILayer
import com.angcyo.ilayer.container.DragRectFConstraint
import com.angcyo.ilayer.container.WindowContainer
import com.angcyo.library.*
import com.angcyo.library.ex.isDebug
import com.angcyo.library.ex.isDebugType
import com.angcyo.library.ex.openApp
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
     * [duration] 转圈时长, 毫秒*/
    fun show(text: CharSequence?, summary: CharSequence? = null, duration: Long = -1) {
        renderLayer = {
            //常亮
            itemView.keepScreenOn = true

            if (duration > 0) {
                v<CircleLoadingView>(R.id.progress_bar)?.setProgress(100, 0, duration)
            } else if (duration == 0L) {
                v<CircleLoadingView>(R.id.progress_bar)?.setProgress(0)
            }
            tv(R.id.text_view)?.text = text

            visible(R.id.catch_button, isDebug())
            visible(R.id.summary_text_view, summary != null)
            tv(R.id.summary_text_view)?.text = summary

            //打开本机程序
            clickItem {
                app().openApp()
            }

            //捕捉界面信息
            throttleClick(R.id.catch_button) {
                doBack {
                    val logWindow = LogWindowAccessibilityInterceptor.logWindow()
                    if (!logWindow.isNullOrEmpty()) {
                        DslFileHelper.write(
                            AccessibilityHelper.logFolderName,
                            "catch.log",
                            logWindow.wrapData()
                        )

                        onSaveWindowLog?.invoke(logWindow)
                    }
                }
            }

            //测试按钮
            visible(R.id.test_button, isDebugType())
            throttleClick(R.id.test_button) {
                //"touch:0.9192,0.9642"
                val p1 = PointF(_screenWidth * 0.9192f, _screenHeight * 0.9642f)
                BaseAccessibilityService.lastService?.gesture?.click(p1.x, p1.y)
                L.w(p1)

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

        show(_windowContainer)
    }

    fun hide() {
        hide(_windowContainer)
    }
}