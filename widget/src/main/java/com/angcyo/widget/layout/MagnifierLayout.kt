package com.angcyo.widget.layout

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.FrameLayout
import androidx.core.graphics.toColorInt
import androidx.core.graphics.withSave
import com.angcyo.drawable.CheckerboardDrawable
import com.angcyo.library.ex.dp
import com.angcyo.library.ex.dpi
import com.angcyo.library.ex.interceptParentTouchEvent

/**
 * 放大镜布局
 * [com.angcyo.core.component.DslMagnifier]
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2023/05/28
 */
class MagnifierLayout(context: Context, attributeSet: AttributeSet? = null) :
    FrameLayout(context, attributeSet) {

    /**放大镜内容*/
    var magnifierCanvas: Canvas? = null
    var magnifierBitmap: Bitmap? = null

    /**放大镜的样式*/
    var magnifierPath = Path()

    /**描边颜色*/
    var magnifierColor = "#C3C3C3".toColorInt()

    /**内圈*/
    var magnifierInsidePath = Path()
    var magnifierPathPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    /**放大镜直径*/
    var magnifierSize: Int = 80 * dpi

    /**放大镜内容X/Y轴偏移的距离*/
    var magnifierOffsetX: Float = 20 * dp
    var magnifierOffsetY: Float = 20 * dp

    /**内圈的直径*/
    var magnifierInsideSize: Float = 20 * dp
    var magnifierStrokeWidth: Float = 2 * dp

    /**放大的倍数*/
    var magnifierScale: Float = 2f

    /**绘制的矩阵*/
    var magnifierMatrix: Matrix = Matrix()

    private val touchPoint = PointF()

    /**透明棋盘*/
    private var alphaDrawable: Drawable? = CheckerboardDrawable.create()

    init {
        setWillNotDraw(false)
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        val result = super.dispatchTouchEvent(ev)
        if (isEnabled) {
            interceptParentTouchEvent(ev)
            touchPoint.set(ev.x, ev.y)
            when (ev.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    if (magnifierBitmap == null) {
                        initMagnifier()
                    }
                    updateMagnifier()
                }

                MotionEvent.ACTION_MOVE -> {
                    updateMagnifier()
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    magnifierBitmap?.recycle()
                    magnifierBitmap = null
                    magnifierCanvas = null
                }
            }
        }
        return result
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        if (isEnabled) {
            drawMagnifier(canvas)
        }
    }

    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val result = super.onTouchEvent(event)
        return isEnabled || result
    }

    //region ---绘制放大镜---

    /**初始化放大镜画布*/
    fun initMagnifier() {
        val size = magnifierSize / 2f

        //内圈path
        magnifierInsidePath.reset()
        magnifierInsidePath.addCircle(
            size,
            size,
            magnifierInsideSize / 2f - magnifierStrokeWidth / 2,
            Path.Direction.CW
        )

        //限制path
        magnifierPath.reset()
        magnifierPath.addCircle(
            size,
            size,
            size,
            Path.Direction.CW
        )

        //画笔样式
        magnifierPathPaint.color = magnifierColor
        magnifierPathPaint.style = Paint.Style.STROKE
        magnifierPathPaint.strokeCap = Paint.Cap.ROUND
        magnifierPathPaint.strokeWidth = magnifierStrokeWidth //有一半会被clip

        //画布
        magnifierBitmap?.recycle()
        Bitmap.createBitmap(magnifierSize, magnifierSize, Bitmap.Config.ARGB_8888)?.let {
            magnifierBitmap = it
            magnifierCanvas = Canvas(it)
        }
    }

    /**手势移动的时候, 更新放大镜中的内容*/
    fun updateMagnifier() {
        magnifierCanvas?.let { canvas ->
            magnifierMatrix.reset()
            val magnifierRadius = magnifierSize / 2f
            magnifierMatrix.setTranslate(-touchPoint.x, -touchPoint.y)
            magnifierMatrix.postScale(magnifierScale, magnifierScale)
            magnifierMatrix.postTranslate(magnifierRadius, magnifierRadius)
            canvas.withSave {
                canvas.clipPath(magnifierPath)
                //透明背景
                alphaDrawable?.draw(this)
                //绘制内容
                withSave {
                    canvas.concat(magnifierMatrix)
                    dispatchDraw(canvas)
                }

                //绘制内圈
                canvas.drawPath(magnifierInsidePath, magnifierPathPaint)

                //绘制外圈
                withSave {
                    val scaleTo = (magnifierRadius - magnifierStrokeWidth / 2) / magnifierRadius
                    canvas.scale(scaleTo, scaleTo, magnifierRadius, magnifierRadius)
                    canvas.drawPath(magnifierPath, magnifierPathPaint)
                }
            }
            postInvalidate()
        }
    }

    /**绘制放大镜的内容*/
    private fun drawMagnifier(canvas: Canvas) {
        magnifierBitmap?.let {
            val x = if (touchPoint.x > measuredWidth / 2) {
                magnifierOffsetX
            } else {
                measuredWidth - magnifierSize - magnifierOffsetX
            }
            val y = if (touchPoint.y > measuredHeight / 2) {
                magnifierOffsetY
            } else {
                measuredHeight - magnifierSize - magnifierOffsetY
            }
            canvas.drawBitmap(it, x, y, magnifierPathPaint)
        }
    }

    //endregion ---绘制放大镜---

}