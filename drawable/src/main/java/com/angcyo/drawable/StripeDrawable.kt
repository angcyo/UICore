package com.angcyo.drawable

import android.graphics.*
import android.graphics.drawable.Drawable
import androidx.core.graphics.withClip
import androidx.core.graphics.withMatrix
import kotlin.math.max

/**
 * 条纹绘制
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/05/11
 */
class StripeDrawable : Drawable() {

    val paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    /**条纹的旋转角度, 非弧度*/
    var rotate: Float = -45f

    /**条纹的高度, 请使用dp单位*/
    var stripeHeight: Float = 20f

    /**条纹间隔的高度, 请使用dp单位*/
    var intervalHeight: Float = 10f

    /**条纹的颜色*/
    var stripeColor: Int = Color.parseColor("#cccccc")

    /**条纹间隔个颜色*/
    var intervalColor: Int = Color.parseColor("#e2e2e2")

    /**边框, 如果设置了边框, 那么就会clip画布, 并且绘制边框*/
    var borderPath: Path? = null

    /**边框的颜色*/
    var borderColor: Int = Color.parseColor("#69686d")

    /**边框的宽度, 请使用dp单位*/
    var borderWidth: Float = 2f

    /**条纹绘制的偏移补偿, 通过动态改变此值可以达到条纹平移的动画效果
     * 不为0时, 会有动画效果*/
    var offsetStep: Float = 0f

    var _offset = 0f
    val _matrix = Matrix()
    val _rotateRect = RectF()

    override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)
        update()
    }

    fun update() {
        val bounds = bounds

        val size = max(bounds.width(), bounds.height()).toFloat()
        _rotateRect.set(
            bounds.left.toFloat(),
            bounds.top.toFloat(),
            bounds.left + size,
            bounds.top + size
        )
        val matrix = _matrix
        matrix.reset()
        matrix.postRotate(rotate, _rotateRect.centerX(), _rotateRect.centerY())
        matrix.mapRect(_rotateRect)

        invalidateSelf()
    }

    override fun draw(canvas: Canvas) {
        val border = borderPath
        if (border == null) {
            drawStripe(canvas)
        } else {
            //有边框
            canvas.withClip(border) {
                drawStripe(canvas)
            }
            paint.style = Paint.Style.STROKE
            paint.color = borderColor
            paint.strokeWidth = borderWidth
            canvas.drawPath(border, paint)
        }

        if (offsetStep != 0f) {
            _offset += offsetStep
            invalidateSelf()
        }
    }

    fun drawStripe(canvas: Canvas) {
        val matrix = _matrix
        paint.style = Paint.Style.FILL
        canvas.withMatrix(matrix) {
            val left = _rotateRect.left
            val top = _rotateRect.top
            val right = _rotateRect.right
            val bottom = _rotateRect.bottom

            drawPositive(canvas, left, top - _offset, right, bottom)
            drawNegative(canvas, left, top - _offset, right, -bottom)
        }
    }

    /**绘制正方向条纹*/
    fun drawPositive(canvas: Canvas, left: Float, top: Float, right: Float, maxBottom: Float) {
        var t = top
        var bottom = t
        var index = 0
        while (bottom <= maxBottom) {
            if (index++ % 2 == 0) {
                paint.color = stripeColor
                bottom += stripeHeight
            } else {
                paint.color = intervalColor
                bottom += intervalHeight
            }
            canvas.drawRect(left, t, right, bottom, paint)
            t = bottom
        }
    }

    /**绘制负方向条纹*/
    fun drawNegative(canvas: Canvas, left: Float, bottom: Float, right: Float, minTop: Float) {
        var b = bottom
        var top = b
        var index = 0
        while (top >= minTop) {
            if (index++ % 2 == 0) {
                paint.color = intervalColor
                top -= intervalHeight
            } else {
                paint.color = stripeColor
                top -= stripeHeight
            }
            canvas.drawRect(left, top, right, b, paint)
            b = top
        }
    }

    override fun setAlpha(alpha: Int) {
        paint.alpha = alpha
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        paint.colorFilter = colorFilter
    }

    @Deprecated(
        "Deprecated in Java",
        ReplaceWith("PixelFormat.OPAQUE", "android.graphics.PixelFormat")
    )
    override fun getOpacity(): Int {
        return PixelFormat.OPAQUE
    }

}