package com.angcyo.widget.layout

import android.graphics.Canvas
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import com.angcyo.library.ex.dp
import com.angcyo.widget.R
import kotlin.math.min

/**
 * clip 布局
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/11/26
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
open class ClipLayoutDelegate : LayoutDelegate() {

    companion object {
        /**不剪切*/
        val CLIP_TYPE_NONE = 0

        /**默认(有一定小圆角的CLIP_TYPE_ROUND)*/
        val CLIP_TYPE_DEFAULT = 1

        /**圆角(可以通过aeqWidth属性, 切换正方形还是长方形)*/
        val CLIP_TYPE_ROUND = 2

        /**圆*/
        val CLIP_TYPE_CIRCLE = 3

        /**直角矩形(无圆角状态)*/
        val CLIP_TYPE_RECT = 4
    }

    private val defaultClipRadius = 3 * dp

    /**当clipType为CLIP_TYPE_CIRCLE时, 这个值表示圆的半径, 否则就是圆角的半径*/
    var clipRadius = defaultClipRadius

    var clipType = CLIP_TYPE_NONE

    private val roundRectF: RectF by lazy {
        RectF()
    }

    private val clipPath: Path by lazy { Path() }

    private val cx
        get() = (paddingLeft + (delegateView.measuredWidth - paddingLeft - paddingRight) / 2).toFloat()

    private val cy
        get() = (paddingTop + (delegateView.measuredHeight - paddingTop - paddingBottom) / 2).toFloat()

    private val cr
        get() = (size / 2).toFloat()

    private val size
        get() = min(
            delegateView.measuredHeight /*- paddingTop - paddingBottom*/,
            delegateView.measuredWidth /*- paddingLeft - paddingRight*/
        )

    override fun initAttribute(view: View, attributeSet: AttributeSet?) {
        super.initAttribute(view, attributeSet)

        val typedArray =
            view.context.obtainStyledAttributes(attributeSet, R.styleable.ClipLayoutDelegate)

        clipType = typedArray.getInt(R.styleable.ClipLayoutDelegate_r_clip_type, clipType)
        clipRadius = typedArray.getDimensionPixelOffset(
            R.styleable.ClipLayoutDelegate_r_clip_radius,
            defaultClipRadius.toInt()
        ).toFloat()

        typedArray.recycle()
    }

    /**布局蒙版*/
    open fun maskLayout(canvas: Canvas, drawSuper: () -> Unit = {}) {
        clipPath.reset()
        when (clipType) {
            CLIP_TYPE_NONE -> {
                //no clip
            }
            CLIP_TYPE_ROUND -> {
                clipPath.addRoundRect(
                    roundRectF, floatArrayOf(
                        clipRadius, clipRadius, clipRadius, clipRadius,
                        clipRadius, clipRadius, clipRadius, clipRadius
                    ), Path.Direction.CW
                )
                canvas.clipPath(clipPath)
            }
            CLIP_TYPE_CIRCLE -> {
                clipPath.addCircle(cx, cy, clipRadius, Path.Direction.CW)
                canvas.clipPath(clipPath)
            }
            CLIP_TYPE_DEFAULT -> {
                clipPath.addRoundRect(
                    roundRectF, floatArrayOf(
                        defaultClipRadius,
                        defaultClipRadius,
                        defaultClipRadius,
                        defaultClipRadius,
                        defaultClipRadius,
                        defaultClipRadius,
                        defaultClipRadius,
                        defaultClipRadius
                    ), Path.Direction.CW
                )
                canvas.clipPath(clipPath)
            }
            CLIP_TYPE_RECT -> {
                clipPath.addRect(roundRectF, Path.Direction.CW)
                canvas.clipPath(clipPath)
            }
        }
        drawSuper()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        roundRectF.set(
            0f,
            0f,
            delegateView.measuredWidth.toFloat(),
            delegateView.measuredHeight.toFloat()
        )
    }
}