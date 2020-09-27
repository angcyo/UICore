package com.angcyo.drawable.skeleton

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.widget.LinearLayout
import com.angcyo.drawable.base.AbsDslDrawable
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
        intArrayOf("#E7E7E7".toColorInt(), "#E2E2E2".toColorInt(), "#E7E7E7".toColorInt())

    /**闪光旋转*/
    var lightRotate = 15f

    /**闪光步长,相对于宽度 */
    var lightStep = 0.12f

    /**激活光线特效*/
    var enableLight = true

    /**是否根据剩余空间, 自动循环补齐*/
    var infiniteMode = true

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

        rootGroup.vertical {
            horizontal {
                group {
                    rect {
                        left = "$r1l w"
                        top = "$r1t w"
                        width = "$r1w w"
                        height = "$r1h w"
                    }
                }
                vertical {
                    rect("2dp") {
                        left = "$r2l w"
                        top = "$r2t w"
                        width = "$r2w w"
                        height = "$r2h w"
                    }
                    rect("2dp") {
                        left = "$r3l w"
                        top = "$r3t w"
                        width = "$r3w w"
                        height = "$r3h w"
                    }
                    rect("2dp") {
                        left = "$r4l w"
                        top = "$r4t w"
                        width = "$r4w w"
                        height = "$r4h w"
                    }
                    rect("2dp") {
                        left = "$r5l w"
                        top = "$r5t w"
                        width = "$r5w w"
                        height = "$r5h w"
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

    fun loadNormalSkeleton2() {
        rootGroup.clear()

        val l = 0.02f
        val t = 0.02f

        val c1r = 0.025f
        val c1l = l + c1r
        val c1t = t + c1r

        val c2r = 0.04
        val c2l = l + c2r
        val c2t = t + c2r

        val r1l = l
        val r1t = t
        val r1w = 0.2f
        val r1h = 0.03f

        val r2l = r1l
        val r2t = r1t + r1h + 0.02
        val r2w = r1w
        val r2h = r1h

        val r3l = r1l
        val r3t = r1t
        val r3w = r1w
        val r3h = r1h

        val r4l = l
        val r4t = t * 2 / 3
        val r4w = 1 - 2 * l - l - c1r * 2
        val r4h = 0.035f

        val r5l = r4l
        val r5t = r1t
        val r5w = r4w / 4
        val r5h = r4h

        val r6l = r5l
        val r6t = r5t * 2
        val r6w = r4w
        val r6h = r4h

        val r7l = r1l
        val r7t = r6t * 2 / 3
        val r7w = r4w / 6
        val r7h = r1h

        val r8w = r4w / 3
        val r8l = 1 - r1l - r8w - l - c1r * 2
        val r8t = r7t
        val r8h = r1h

        rootGroup.vertical {
            horizontal {
                group {
                    circle("$c1r") {
                        left = "$c1l w"
                        top = "$c1t w"
                    }
                }
                vertical {
                    horizontal {
                        group {
                            circle("$c2r") {
                                left = "$c2l w"
                                top = "$c2t w"
                            }
                        }
                        horizontal {
                            vertical {
                                rect("2dp") {
                                    left = "$r1l w"
                                    top = "$r1t w"
                                    width = "$r1w w"
                                    height = "$r1h w"
                                }
                                rect("2dp") {
                                    left = "$r2l w"
                                    top = "$r2t w"
                                    width = "$r2w w"
                                    height = "$r2h w"
                                }
                            }
                            group {
                                rect("2dp") {
                                    left = "$r3l w"
                                    top = "$r3t w"
                                    width = "$r3w w"
                                    height = "$r3h w"
                                }
                            }
                        }
                    }
                    group {
                        rect("2dp") {
                            left = "$r4l w"
                            top = "$r4t w"
                            width = "$r4w w"
                            height = "$r4h w"
                        }
                    }
                    group {
                        rect("2dp") {
                            left = "$r5l w"
                            top = "$r5t w"
                            width = "$r5w w"
                            height = "$r5h w"
                        }
                    }
                    group {
                        rect("2dp") {
                            left = "$r6l w"
                            top = "$r6t w"
                            width = "$r6w w"
                            height = "$r6h w"
                        }
                    }
                    group {
                        rect("2dp") {
                            left = "$r7l w"
                            top = "$r7t w"
                            width = "$r7w w"
                            height = "$r7h w"
                        }
                        rect("2dp") {
                            left = "$r8l w"
                            top = "$r8t w"
                            width = "$r8w w"
                            height = "$r8h w"
                        }
                    }
                }
            }
            group {
                line {
                    left = "$l w"
                    top = "$t w"
                    width = "${1 - l * 2} w"
                }
            }
        }
    }

    fun render(dsl: SkeletonGroupBean.() -> Unit) {
        rootGroup.apply {
            clear()
            apply(dsl)
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
            floatArrayOf(0.3f, 0.5f, 0.7f),
            Shader.TileMode.CLAMP
        )
        _linearGradientMatrix = Matrix()
    }

    //当前布局的坐标参数
    val _layoutParams = LayoutParams()
    override fun draw(canvas: Canvas) {
        if (enableLight) {
            textPaint.shader = _linearGradient
        } else {
            textPaint.shader = null
        }

        _layoutParams.reset()
        _drawGroup(canvas, _layoutParams, rootGroup)
        if (infiniteMode && !rootGroup.groupList.isNullOrEmpty()) {
            if (rootGroup.orientation == LinearLayout.VERTICAL) {
                var top = _layoutParams.top + _layoutParams.useHeight
                while (top < viewHeight - paddingBottom) {
                    _layoutParams.reset()
                    _layoutParams.top = top
                    _drawGroup(canvas, _layoutParams, rootGroup)
                    top += _layoutParams.useHeight
                }
            } else if (rootGroup.orientation == LinearLayout.HORIZONTAL) {
                var left = _layoutParams.left
                while (left < viewWidth - paddingRight) {
                    _layoutParams.reset()
                    _layoutParams.left = left
                    _drawGroup(canvas, _layoutParams, rootGroup)
                    left += _layoutParams.useWidth
                }
            }
        }

        if (enableLight) {
            if (_translate <= 2 * viewWidth) {
                _translate += (viewWidth * lightStep).toInt()
            } else {
                _translate = -viewWidth
            }

            _linearGradientMatrix?.setTranslate(_translate.toFloat(), 0f)
            _linearGradientMatrix?.postRotate(lightRotate)
            _linearGradient?.setLocalMatrix(_linearGradientMatrix)

            invalidateSelf()
        }
    }

    val _groupLayoutParams = LayoutParams()
    fun _drawGroup(canvas: Canvas, layoutParams: LayoutParams, groupBean: SkeletonGroupBean) {

        groupBean.skeletonList?.forEach {
            _drawSkeleton(canvas, layoutParams, it)
        }

        if (groupBean.groupList.isNullOrEmpty()) {
            return
        }

        val orientation = groupBean.orientation

        var groupLeft = layoutParams.left
        var groupTop = layoutParams.top

        var maxUsedWidth = 0f
        var maxUsedHeight = 0f

        var allUseWidth = 0f
        var allUseHeight = 0f

        groupBean.groupList?.forEach {
            _groupLayoutParams.left = groupLeft
            _groupLayoutParams.top = groupTop
            _drawGroup(canvas, _groupLayoutParams, it)

            allUseWidth += _groupLayoutParams.useWidth
            allUseHeight += _groupLayoutParams.useHeight

            maxUsedWidth = max(maxUsedWidth, _groupLayoutParams.useWidth)
            maxUsedHeight = max(maxUsedHeight, _groupLayoutParams.useHeight)

            //根据方向, 进行位置偏移
            if (orientation == LinearLayout.VERTICAL) {
                groupTop += _groupLayoutParams.useHeight

                if (groupTop > viewHeight) {
                    return@forEach
                }
            } else if (orientation == LinearLayout.HORIZONTAL) {
                groupLeft += _groupLayoutParams.useWidth

                if (groupLeft > viewWidth) {
                    return@forEach
                }
            } else {
                if (groupLeft > viewWidth || groupTop > viewHeight) {
                    return@forEach
                }
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

    fun _drawSkeleton(canvas: Canvas, layoutParams: LayoutParams, skeletonBean: SkeletonBean) {
        textPaint.color = skeletonBean.fillColor
        textPaint.style = Paint.Style.FILL
        textPaint.strokeWidth = 0f

        val useLeft = skeletonBean.left.layoutSize(viewDrawWidth, viewDrawHeight, viewDrawWidth)
        val useTop = skeletonBean.top.layoutSize(viewDrawWidth, viewDrawHeight, viewDrawHeight)

        var left = layoutParams.left + useLeft
        var top = layoutParams.top + useTop
        val width = skeletonBean.width.layoutSize(viewDrawWidth, viewDrawHeight, viewDrawWidth)
        val height = skeletonBean.height.layoutSize(viewDrawWidth, viewDrawHeight, viewDrawHeight)

        val size = skeletonBean.size.layoutSize(viewDrawWidth, viewDrawHeight, viewDrawWidth)

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
                val cx = left
                val cy = top
                canvas.drawCircle(cx, cy, size, textPaint)

                layoutParams.useWidth = useLeft + size
                layoutParams.useHeight = useTop + size
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

    fun LayoutParams.reset() {
        left = paddingLeft.toFloat()
        top = paddingTop.toFloat()
        useWidth = 0f
        useHeight = 0f
    }
}