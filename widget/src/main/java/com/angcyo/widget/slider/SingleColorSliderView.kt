package com.angcyo.widget.slider

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import com.angcyo.library.ex.evaluateColor
import com.angcyo.library.ex.linearGradientShader
import com.angcyo.library.ex.toRectF

/**
 * 单一颜色滑块选择控件
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/06/28
 */
class SingleColorSliderView(context: Context, attributeSet: AttributeSet? = null) :
    BaseSliderView(context, attributeSet) {

    /**当前的颜色*/
    var currentColor: Int = Color.TRANSPARENT

    /**开始的颜色*/
    var startColor: Int = Color.WHITE

    /**结束的颜色*/
    var endColor: Int = Color.BLACK

    /**回调监听*/
    var onColorChangedListener: OnColorChangedListener? = null

    //缓存
    var _panelCache: BitmapCache? = null

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        _panelCache = null

        if (currentColor != Color.TRANSPARENT) {
            for (p in 0..100) {
                val c = evaluateColor(p / 100f, startColor, endColor)
                if (currentColor == c) {
                    sliderProgress = p
                    onColorChangedListener?.onColorChanged(currentColor, false)
                }
            }
        }
    }

    override fun drawPanel(canvas: Canvas) {
        if (_panelCache == null) {
            _panelCache = BitmapCache().also { cache ->
                cache.bitmap = Bitmap.createBitmap(
                    panelRect.width(),
                    panelRect.height(),
                    Bitmap.Config.ARGB_8888
                ).apply {
                    val linePaint = Paint()
                    linePaint.strokeWidth = 0f
                    val rectF = panelRect.toRectF()
                    linePaint.shader = linearGradientShader(
                        0f,
                        0f,
                        rectF.width(),
                        0f,
                        intArrayOf(startColor, endColor)
                    )
                    cache.canvas = Canvas(this).apply {
                        drawRect(0f, 0f, rectF.width(), rectF.height(), linePaint)
                    }
                }
            }
        }
        canvas.drawBitmap(_panelCache!!.bitmap!!, null, panelRect, null)
    }

    override fun _onTouchMoveTo(x: Float, y: Float, isFinish: Boolean) {
        super._onTouchMoveTo(x, y, isFinish)
        currentColor = evaluateColor(sliderProgress / 100f, startColor, endColor)
        onColorChangedListener?.onColorChanged(currentColor, isFinish)
    }

    /**回调*/
    interface OnColorChangedListener {
        fun onColorChanged(newColor: Int, fromUser: Boolean)
    }

    //缓存
    class BitmapCache {
        var canvas: Canvas? = null
        var bitmap: Bitmap? = null
        var value = 0f
    }
}