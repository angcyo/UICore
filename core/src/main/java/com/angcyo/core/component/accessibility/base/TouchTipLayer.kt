package com.angcyo.core.component.accessibility.base

import android.graphics.Color
import android.view.WindowManager
import com.angcyo.core.R
import com.angcyo.drawable.skeleton.circle
import com.angcyo.drawable.skeleton.line
import com.angcyo.ilayer.ILayer
import com.angcyo.ilayer.container.OffsetPosition
import com.angcyo.ilayer.container.WindowContainer
import com.angcyo.library.app
import com.angcyo.library.component.MainExecutor
import com.angcyo.library.ex._color
import com.angcyo.library.ex.abs
import com.angcyo.widget.SkeletonView

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/09/24
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
object TouchTipLayer : ILayer() {

    val _windowContainer = WindowContainer(app()).apply {
        defaultOffsetPosition = OffsetPosition(offsetX = 0f, offsetY = 0f)
        wmLayoutParams.apply {
            width = -1
            height = -1
            flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or  //不获取焦点
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or  //不接受触摸屏事件
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN  //占满整个屏幕
        }
    }

    val _hideRunnable: Runnable = Runnable {
        hide(_windowContainer)
    }

    /**多少毫秒后, 自动隐藏*/
    var showTime: Long = 800

    /**圆点的半径*/
    var cr = "0.01"
    var cColor = _color(R.color.colorAccent)
    var lineColor = Color.GREEN

    init {
        iLayerLayoutId = R.layout.lib_layout_touch_tip
        enableDrag = false
        autoRestorePosition = false
    }

    fun _show() {
        MainExecutor.handler.removeCallbacks(_hideRunnable)

        if (showTime > 0) {
            show(_windowContainer)
            MainExecutor.handler.postDelayed(_hideRunnable, showTime)
        }
    }

    /**显示touch的提示, 横竖一根线.
     * [x] [y] 请使用比例值*/
    fun showTouch(x: Float, y: Float) {
        renderLayer = {
            v<SkeletonView>(R.id.lib_skeleton_view)?.skeletonDrawable {
                infiniteMode = false
                enableLight = false
                render {
                    line {
                        left = "0"
                        top = "$y"
                        width = "0.9999"
                        fillColor = lineColor
                    }
                    line {
                        left = "$x"
                        top = "0"
                        height = "0.9999"
                        fillColor = lineColor
                    }
                    circle(cr) {
                        left = "$x"
                        top = "$y"
                        fillColor = cColor
                    }
                }
            }
        }
        _show()
    }

    /**显示移动提示*/
    fun showMove(x1: Float, y1: Float, x2: Float, y2: Float) {
        renderLayer = {
            v<SkeletonView>(R.id.lib_skeleton_view)?.skeletonDrawable {
                infiniteMode = false
                enableLight = false
                render {
                    var cl = 0f
                    var ct = 0f
                    line {
                        fillColor = lineColor
                        //是否是横向
                        val isHorizontal = (x2 - x1).abs() > (y2 - y1).abs()

                        if (isHorizontal) {
                            //从左到右
                            val ltr = x2 > x1
                            top = "$y1"
                            if (ltr) {
                                left = "$x1"
                                cl = x2
                                ct = y2
                            } else {
                                left = "$x2"
                                cl = x1
                                ct = y1
                            }
                            width = "${(x2 - x1).abs()}"
                        } else {
                            //从下到上
                            val btt = y2 < y1

                            left = "$x1"

                            if (btt) {
                                top = "$y2"
                                cl = x2
                                ct = y2
                            } else {
                                top = "$y1"
                                cl = x1
                                ct = y1
                            }

                            height = "${(y2 - y1).abs()}"
                        }
                    }
                    circle("0.01") {
                        left = "$cl"
                        top = "$ct"
                        fillColor = cColor
                    }
                }
            }
        }
        _show()
    }
}