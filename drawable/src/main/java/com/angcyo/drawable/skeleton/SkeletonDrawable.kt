package com.angcyo.drawable.skeleton

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.widget.LinearLayout
import com.angcyo.drawable.base.AbsDslDrawable
import com.angcyo.library.ex.loop
import com.angcyo.library.ex.toColorInt
import kotlin.math.max

/**
 * 骨架[Drawable]
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/09/22
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */

class SkeletonDrawable : AbsDslDrawable() {

    /**根*/
    val rootGroup = SkeletonGroupBean(orientation = LinearLayout.VERTICAL)

    /**闪光颜色*/
    var lightColors =
        intArrayOf("#E7E7E7".toColorInt(), "#D7D7D7".toColorInt(), "#E7E7E7".toColorInt())

    /**闪光旋转*/
    var lightRotate = 15f

    /**激活光线特效*/
    var enableLight = true

    init {
        loadNormalSkeleton()
//        loadNormalSkeleton2()
    }

    override fun initAttribute(context: Context, attributeSet: AttributeSet?) {
        super.initAttribute(context, attributeSet)
    }

    fun loadNormalSkeleton() {
        rootGroup.clear()
        val r1l = 0.02f
        val r1t = 0.02f
        val r1w = 0.2f
        val r1h = 0.18f

        val r2l = r1l
        val r2t = r1t
        val r2w = 1 - 2 * r1l - r2l - r1w
        val r2h = 0.04f

        val r3l = r1l
        val r3t = r2t + r2h + r1t / 2
        val r3w = (1 - 2 * r1l - r3l) / 3
        val r3h = r2h

        val r4l = r1l
        val r4h = r2h
        val r4t = r1h + r1t - r4h
        val r4w = (1 - 2 * r1l - r3l) / 6

        val r5w = r4w
        val r5l = 1 - r1l - r2l - r5w - r1w
        val r5h = r2h
        val r5t = r1h + r1t - r5h

        loop(8) {
            rootGroup.vertical {
                horizontal {
                    group {
                        rect {
                            left = "$r1l"
                            top = "$r1t"
                            width = "$r1w"
                            height = "$r1h"
                        }
                    }
                    vertical {
                        rect("2dp") {
                            left = "$r2l"
                            top = "$r2t"
                            width = "$r2w"
                            height = "$r2h"
                        }
                        rect("2dp") {
                            left = "$r3l"
                            top = "$r3t"
                            width = "$r3w"
                            height = "$r3h"
                        }
                        rect("2dp") {
                            left = "$r4l"
                            top = "$r4t"
                            width = "$r4w"
                            height = "$r4h"
                        }
                        rect("2dp") {
                            left = "$r5l"
                            top = "$r5t"
                            width = "$r5w"
                            height = "$r5h"
                        }
                    }
                }
                group {
                    line {
                        left = "$r1l"
                        top = "$r1t"
                        width = "${1 - r1l * 2}"
                    }
                }
            }
        }
    }

    fun loadNormalSkeleton2() {
        rootGroup.clear()

        val c1r = 0.03f
        val c1l = 0.02f
        val c1t = 0.02f

        val c2r = 0.1
        val c2l = c1l
        val c2t = c1t

        loop(5) {
            rootGroup.vertical {
                horizontal {
                    group {
                        circle("$c1r") {
                            left = "$c1l"
                            top = "$c1t"
                        }
                    }
                    vertical {
                        horizontal {
                            circle("$c2r") {
                                left = "$c2l"
                                top = "$c2t"
                            }
                        }
                    }
                }
                group {
                    line {

                    }
                }
            }
        }
    }

    var _linearGradient: LinearGradient? = null
    var _linearGradientMatrix: Matrix? = null
    var _translate: Int = 0

    override fun onBoundsChange(bounds: Rect?) {
        super.onBoundsChange(bounds)
        _linearGradient = LinearGradient(
            -viewWidth.toFloat(),
            0f,
            0f,
            0f,
            lightColors,
            floatArrayOf(0.4f, 0.5f, 0.6f),
            Shader.TileMode.CLAMP
        )
        _linearGradientMatrix = Matrix()
    }

    override fun draw(canvas: Canvas) {
        if (enableLight) {
            textPaint.shader = _linearGradient
        } else {
            textPaint.shader = null
        }

        //当前布局的坐标参数
        val layoutParams = LayoutParams()
        _drawGroup(canvas, layoutParams, rootGroup)

        if (enableLight) {
            if (_translate <= 2 * viewWidth) {
                _translate += viewWidth / 10
            } else {
                _translate = -viewWidth
            }

            _linearGradientMatrix?.setTranslate(_translate.toFloat(), 0f)
            _linearGradientMatrix?.postRotate(lightRotate)
            _linearGradient?.setLocalMatrix(_linearGradientMatrix)

            invalidateSelf()
        }
    }

    fun _drawGroup(
        canvas: Canvas,
        layoutParams: LayoutParams,
        groupBean: SkeletonGroupBean
    ) {

        groupBean.skeletonList?.forEach {
            _drawSkeleton(canvas, layoutParams, it)
        }

        if (groupBean.groupList.isNullOrEmpty()) {
            return
        }

        val orientation = groupBean.orientation
        val groupLayoutParams = LayoutParams(layoutParams.left, layoutParams.top)

        var maxUsedWidth = 0f
        var maxUsedHeight = 0f

        var allUseWidth = 0f
        var allUseHeight = 0f

        groupBean.groupList?.forEach {
            _drawGroup(canvas, groupLayoutParams, it)

            allUseWidth += groupLayoutParams.useWidth
            allUseHeight += groupLayoutParams.useHeight

            maxUsedWidth = max(maxUsedWidth, groupLayoutParams.useWidth)
            maxUsedHeight = max(maxUsedHeight, groupLayoutParams.useHeight)

            //根据方向, 进行位置偏移
            if (orientation == LinearLayout.VERTICAL) {
                groupLayoutParams.top += groupLayoutParams.useHeight
            } else if (orientation == LinearLayout.HORIZONTAL) {
                groupLayoutParams.left += groupLayoutParams.useWidth
            }

            if (groupLayoutParams.top > viewHeight) {
                return@forEach
            }
        }

        //外
        if (orientation == LinearLayout.VERTICAL) {
            layoutParams.useWidth = maxUsedWidth
            layoutParams.useHeight = allUseHeight
        } else if (orientation == LinearLayout.HORIZONTAL) {
            layoutParams.useWidth = allUseWidth
            layoutParams.useHeight = maxUsedHeight
        } else {
            layoutParams.useWidth = maxUsedWidth
            layoutParams.useHeight = maxUsedHeight
        }
    }

    fun _drawSkeleton(
        canvas: Canvas,
        layoutParams: LayoutParams,
        skeletonBean: SkeletonBean
    ) {
        textPaint.color = skeletonBean.fillColor
        textPaint.style = Paint.Style.FILL
        textPaint.strokeWidth = 0f

        val useLeft = skeletonBean.left.layoutSize(viewWidth, viewHeight)
        val useTop = skeletonBean.top.layoutSize(viewWidth, viewHeight)

        var left = layoutParams.left + useLeft
        var top = layoutParams.top + useTop
        val width = skeletonBean.width.layoutSize(viewWidth, viewHeight)
        val height = skeletonBean.height.layoutSize(viewWidth, viewHeight)

        val size = skeletonBean.size.layoutSize(viewWidth, viewHeight)

        layoutParams.useWidth = 0f
        layoutParams.useHeight = 0f

        when (skeletonBean.type) {
            SkeletonBean.SKELETON_TYPE_LINE -> {
                textPaint.strokeWidth = size
                //画线
                if (width > 0) {
                    top += textPaint.strokeWidth / 2
                    canvas.drawLine(
                        left,
                        top,
                        left + width,
                        top,
                        textPaint
                    )

                    layoutParams.useWidth = width + useLeft
                } else {
                    layoutParams.useWidth = size + useLeft
                }

                if (height > 0) {
                    textPaint.strokeWidth = size

                    left += textPaint.strokeWidth / 2
                    canvas.drawLine(
                        left,
                        top,
                        left,
                        top + height,
                        textPaint
                    )

                    layoutParams.useHeight = height + useTop
                } else {
                    layoutParams.useHeight = size + useTop
                }
            }
            SkeletonBean.SKELETON_TYPE_CIRCLE -> {
                //画圆
                val cx = left + size
                val cy = top + size
                canvas.drawCircle(cx, cy, size, textPaint)

                layoutParams.useWidth = cx + size
                layoutParams.useHeight = cy + size
            }
            SkeletonBean.SKELETON_TYPE_RECT -> {
                //画矩形
                canvas.drawRoundRect(left, top, left + width, top + height, size, size, textPaint)

                layoutParams.useWidth = width + useLeft
                layoutParams.useHeight = height + useTop
            }
        }
    }

    data class LayoutParams(
        var left: Float = 0f,
        var top: Float = 0f,
        //布局完之后, 使用了多少宽度和高度
        var useWidth: Float = 0f,
        var useHeight: Float = 0f
    )
}