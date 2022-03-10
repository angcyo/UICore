package com.angcyo.core.component

import android.content.Context
import android.graphics.*
import android.view.*
import androidx.core.graphics.toColorInt

/**
 * 放大镜, 可以用来方法任意控件
 *
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/03/09
 */
class DslMagnifier {

    var _containerLayout: ViewGroup? = null
    var _targetView: View? = null
    var magnifierView: MagnifierView? = null

    /**在指定的[viewGroup]上附加放大镜
     * [targetView]放大镜的内容*/
    fun init(viewGroup: ViewGroup, targetView: View) {
        _containerLayout = viewGroup
        _targetView = targetView
        magnifierView = MagnifierView(viewGroup.context)
    }

    fun attach() {
        _containerLayout?.let { viewGroup ->
            if (magnifierView?.parent == null) {
                viewGroup.addView(
                    magnifierView,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }
        }
    }

    /**移除*/
    fun detach() {
        try {
            magnifierView?.let {
                _containerLayout?.removeView(it)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**在指定坐标, 显示放大镜
     * [x] 距离屏幕的x坐标
     * [y] 距离屏幕的y坐标*/
    fun show(touchEvent: MotionEvent) {
        attach()
        magnifierView?.update(touchEvent)
    }

    /**隐藏放大镜*/
    fun hide() {
        detach()
        magnifierView?.isEnabled = false
    }

    inner class MagnifierView(context: Context) : View(context) {

        /**放大镜内容*/
        var magnifierCanvas: Canvas? = null
        var magnifierBitmap: Bitmap? = null

        /**描边*/
        var magnifierPath = Path()

        /**描边颜色*/
        var magnifierColor = "#C3C3C3".toColorInt()

        /**内圈*/
        var magnifierInsidePath = Path()
        var magnifierPathPaint = Paint(Paint.ANTI_ALIAS_FLAG)

        /**放大镜直径*/
        var magnifierSize: Int = 0

        /**内圈的直径*/
        var magnifierInsideSize: Float = 0f
        var magnifierStrokeWidth: Float = 0f

        /**放大的倍数*/
        var magnifierScale: Float = 2f

        /**绘制的矩阵*/
        var magnifierMatrix: Matrix = Matrix()

        init {
            val density = context.resources.displayMetrics.density
            val size = 100 * density.toInt()
            magnifierSize = size
            magnifierStrokeWidth = 2 * density
            magnifierInsideSize = 20 * density
        }

        /**初始化放大镜*/
        fun initMagnifier() {
            magnifierInsidePath.reset()
            magnifierInsidePath.addCircle(
                magnifierSize / 2f,
                magnifierSize / 2f,
                magnifierInsideSize / 2f - magnifierStrokeWidth / 2,
                Path.Direction.CW
            )

            magnifierPath.reset()
            magnifierPath.addCircle(
                magnifierSize / 2f,
                magnifierSize / 2f,
                magnifierSize / 2f,
                Path.Direction.CW
            )

            magnifierPathPaint.color = magnifierColor
            magnifierPathPaint.style = Paint.Style.STROKE
            magnifierPathPaint.strokeWidth = magnifierStrokeWidth //有一半会被clip

            magnifierBitmap?.recycle()
            magnifierBitmap =
                Bitmap.createBitmap(magnifierSize, magnifierSize, Bitmap.Config.ARGB_8888).apply {
                    magnifierCanvas = Canvas(this)
                }
        }

        /**手势touch的位置(距离屏幕), 用来决定放大镜绘制的位置*/
        var targetTouchX: Float = 0f
        var targetTouchY: Float = 0f

        fun update(touchEvent: MotionEvent) {
            isEnabled = true
            if (magnifierCanvas == null) {
                initMagnifier()
            }
            _targetView?.let {
                magnifierCanvas?.let { canvas ->
                    val x = touchEvent.x
                    val y = touchEvent.y

                    targetTouchX = touchEvent.rawX
                    targetTouchY = touchEvent.rawY

                    val magnifierRadius = magnifierSize / 2f

                    canvas.save()
                    canvas.clipPath(magnifierPath)
                    canvas.drawColor(Color.WHITE)

                    magnifierMatrix.reset()
                    magnifierMatrix.setTranslate(-x, -y)
                    magnifierMatrix.postScale(magnifierScale, magnifierScale)
                    magnifierMatrix.postTranslate(magnifierRadius, magnifierRadius)
                    canvas.concat(magnifierMatrix)

                    it.draw(canvas)
                    canvas.restore()

                    //绘制内圈
                    canvas.drawPath(magnifierInsidePath, magnifierPathPaint)

                    //绘制外圈
                    canvas.save()
                    val scaleTo = (magnifierRadius - magnifierStrokeWidth / 2) / magnifierRadius
                    canvas.scale(scaleTo, scaleTo, magnifierRadius, magnifierRadius)
                    canvas.drawPath(magnifierPath, magnifierPathPaint)
                    canvas.restore()

                    invalidate()
                }
            }
        }

        override fun onDetachedFromWindow() {
            magnifierBitmap?.recycle()
            magnifierBitmap = null
            magnifierCanvas = null
            super.onDetachedFromWindow()
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            if (isEnabled) {
                magnifierBitmap?.let {
                    //canvas.drawBitmap(it, measuredWidth / 2f, measuredHeight / 2f, null)
                    canvas.save()
                    canvas.drawBitmap(
                        it,
                        targetTouchX - magnifierSize / 2,
                        targetTouchY - magnifierSize * 2,
                        null
                    )
                    canvas.restore()
                }
            }
        }
    }

}