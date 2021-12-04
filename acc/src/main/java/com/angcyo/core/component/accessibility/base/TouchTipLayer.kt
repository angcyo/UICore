package com.angcyo.core.component.accessibility.base

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.WindowManager
import com.angcyo.acc.R
import com.angcyo.drawable.skeleton.circle
import com.angcyo.drawable.skeleton.line
import com.angcyo.ilayer.ILayer
import com.angcyo.ilayer.container.OffsetPosition
import com.angcyo.ilayer.container.WindowContainer
import com.angcyo.library._contentHeight
import com.angcyo.library.app
import com.angcyo.library.component.MainExecutor
import com.angcyo.library.ex._color
import com.angcyo.library.ex.abs
import com.angcyo.library.ex.alphaRatio
import com.angcyo.widget.SkeletonView
import kotlin.math.min

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/09/24
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */

@SuppressLint("StaticFieldLeak")
object TouchTipLayer : ILayer() {

    val _windowContainer = WindowContainer(app()).apply {
        defaultOffsetPosition = OffsetPosition(offsetX = 0f, offsetY = 0f)
        wmLayoutParams.apply {
            width = -1
            height = _contentHeight
            flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or    //不获取焦点
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or    //不接受触摸屏事件
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or //占满整个屏幕
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS    //允许到屏幕之外
        }
    }

    val _hideRunnable: Runnable = Runnable {
        hide(_windowContainer)
    }

    /**多少毫秒后, 自动隐藏*/
    var showTime: Long = 800
    /** 10*/

    /**圆点的半径*/
    var cr = 0.012f
    var lineWidth = 2f
    var lineWidth2 = lineWidth * 2.5
    var cColor = _color(R.color.colorAccent)
    var lineColor = cColor.alphaRatio(0.8f)//Color.GREEN                        //"92C3FE".toColor()
    var bgColor = Color.WHITE.alphaRatio(0.8f) //_color(R.color.transparent40)

    init {
        iLayerLayoutId = R.layout.lib_layout_touch_tip
        enableDrag = true
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
            post {
                _windowContainer.updateLayout(this@TouchTipLayer)
            }
            v<SkeletonView>(R.id.lib_skeleton_view)?.skeletonDrawable {
                infiniteMode = false
                enableLight = false
                render {
                    line {
                        left = "0"
                        top = "$y"
                        width = "0.9999"
                        size = "${lineWidth}dp"
                        fillColor = lineColor
                    }
                    line {
                        left = "$x"
                        top = "0"
                        height = "0.9999"
                        size = "${lineWidth}dp"
                        fillColor = lineColor
                    }
                    circle("$cr") {
                        left = "$x"
                        top = "$y"
                        fillColor = cColor
                    }
                }
            }
        }
        _show()
    }

    /**显示移动提示
     *  [x] [y] 请使用比例值*/
    fun showMove(x1: Float, y1: Float, x2: Float, y2: Float) {
        renderLayer = {
            post {
                _windowContainer.updateLayout(this@TouchTipLayer)
            }
            v<SkeletonView>(R.id.lib_skeleton_view)?.skeletonDrawable {
                infiniteMode = false
                enableLight = false
                render {
                    var cl = 0f
                    var ct = 0f
                    val isHorizontal = (x2 - x1).abs() > (y2 - y1).abs()

                    val t: String
                    val l: String

                    //是否是横向
                    if (isHorizontal) {
                        //从左到右
                        t = "$y1"
                        l = "${min(x1, x2)}"
                        cl = x2
                        ct = y2
                    } else {
                        l = "$x1"
                        t = "${min(y1, y2)}"
                        cl = x2
                        ct = y2
                    }

                    //bg
                    line {
                        fillColor = bgColor
                        size = "${lineWidth2}dp"
                        top = t
                        left = l
                        round = size

                        //是否是横向
                        if (isHorizontal) {
                            //从左到右
                            width = "${(x2 - x1).abs()}"
                        } else {
                            height = "${(y2 - y1).abs()}"
                        }
                    }
                    circle("${cr * 2}") {
                        left = "$cl"
                        top = "$ct"
                        fillColor = bgColor
                    }

                    //raw
                    line {
                        fillColor = lineColor
                        size = "${lineWidth}dp"
                        top = t
                        left = l
                        round = size

                        //是否是横向
                        if (isHorizontal) {
                            offsetX = size
                            //从左到右
                            width = "${(x2 - x1).abs()}"
                        } else {
                            offsetY = size
                            height = "${(y2 - y1).abs()}"
                        }
                    }
                    circle("$cr") {
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