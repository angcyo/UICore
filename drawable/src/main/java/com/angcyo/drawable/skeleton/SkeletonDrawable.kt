package com.angcyo.drawable.skeleton

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.widget.LinearLayout
import com.angcyo.drawable.base.AbsDslDrawable
import kotlin.math.max

/**
 * 骨架[Drawable]
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/09/22
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */

class SkeletonDrawable : AbsDslDrawable() {

    val rootGroup = SkeletonGroupBean()

    init {
        rootGroup.group {
            horizontal {
                group {
                    rect {
                        left = "0.02"
                        top = "0.02"
                        width = "0.2"
                        height = "0.15"
                    }
                }
                vertical {
                    rect {
                        left = "0.02"
                        top = "0.02"
                        width = "0.7"
                        height = "0.1"
                    }
                }
            }
        }

        rootGroup.group {
            line {
                left = "0.1w"
                top = "0.1w"
                width = "0.5w"
                height = "0.5w"
            }
        }
    }

    override fun initAttribute(context: Context, attributeSet: AttributeSet?) {
        super.initAttribute(context, attributeSet)
    }

    override fun draw(canvas: Canvas) {
        //当前布局的坐标参数
        val layoutParams = LayoutParams()

        _drawGroup(canvas, layoutParams, rootGroup)
    }

    fun _drawGroup(canvas: Canvas, layoutParams: LayoutParams, groupBean: SkeletonGroupBean) {
        val groupLayoutParams = LayoutParams(layoutParams.left, layoutParams.top)

        groupBean.skeletonList?.forEach {
            _drawSkeleton(canvas, groupLayoutParams, it)
        }

        groupBean.groupList?.forEach {
            _drawGroup(canvas, groupLayoutParams, it)

            //内
            if (it.orientation == LinearLayout.VERTICAL) {
                groupLayoutParams.top += groupLayoutParams.useHeight
            } else if (it.orientation == LinearLayout.HORIZONTAL) {
                groupLayoutParams.left += groupLayoutParams.useWidth
            }
        }

        //外
        if (groupBean.orientation == LinearLayout.VERTICAL) {
            layoutParams.top += groupLayoutParams.useHeight
        } else if (groupBean.orientation == LinearLayout.HORIZONTAL) {
            layoutParams.left += groupLayoutParams.useWidth
        }
    }

    fun _drawSkeleton(canvas: Canvas, layoutParams: LayoutParams, skeletonBean: SkeletonBean) {
        textPaint.color = skeletonBean.fillColor
        textPaint.style = Paint.Style.FILL
        textPaint.strokeWidth = 0f

        val useLeft = skeletonBean.left.layoutSize(viewWidth, viewHeight)
        val useTop = skeletonBean.top.layoutSize(viewWidth, viewHeight)

        var left = layoutParams.left + useLeft
        var top = layoutParams.top + useTop
        val width = skeletonBean.width.layoutSize(viewWidth, viewHeight)
        val height = skeletonBean.height.layoutSize(viewWidth, viewHeight)

        when (skeletonBean.type) {
            SkeletonBean.SKELETON_TYPE_LINE -> {
                //画线
                if (width > 0) {
                    textPaint.strokeWidth = skeletonBean.size.layoutSize(viewWidth, viewHeight)

                    top += textPaint.strokeWidth / 2
                    canvas.drawLine(
                        left,
                        top,
                        left + width,
                        top,
                        textPaint
                    )

                    layoutParams.useWidth = max(layoutParams.useWidth, width + useLeft)
                }

                if (height > 0) {
                    textPaint.strokeWidth = skeletonBean.size.layoutSize(viewWidth, viewHeight)

                    left += textPaint.strokeWidth / 2
                    canvas.drawLine(
                        left,
                        top,
                        left,
                        top + height,
                        textPaint
                    )

                    layoutParams.useHeight = max(layoutParams.useHeight, height + useTop)
                }
            }
            SkeletonBean.SKELETON_TYPE_CIRCLE -> {
                //画圆
            }
            SkeletonBean.SKELETON_TYPE_RECT -> {
                //画矩形
                val size = skeletonBean.size.layoutSize(viewWidth, viewHeight)
                canvas.drawRoundRect(left, top, left + width, top + height, size, size, textPaint)

                layoutParams.useWidth = max(layoutParams.useWidth, width + useLeft)
                layoutParams.useHeight = max(layoutParams.useHeight, height + useTop)
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