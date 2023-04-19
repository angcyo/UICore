package com.angcyo.dsladapter

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import androidx.recyclerview.widget.RecyclerView
import com.angcyo.library._refreshRateRatio
import com.angcyo.library.ex._color
import com.angcyo.library.ex.alphaRatio
import com.angcyo.library.ex.createPaint
import com.angcyo.library.ex.getChildOrNull
import com.angcyo.library.ex.linearHorizontalGradientShader
import com.angcyo.widget.R
import kotlin.math.min

/**
 * 在Item上绘制动画
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/04/19
 */
class RecyclerItemFlowAnimator(
    val startChildIndex: Int /*开始的child位置, 支持负数*/,
    val endChildIndex: Int /*结束的child位置, 支持负数*/
) : RecyclerView.ItemDecoration() {

    /**开始动画*/
    fun start(recyclerView: RecyclerView?) {
        recyclerView?.addItemDecoration(this)
        _recyclerView = recyclerView
        recyclerView?.postInvalidate()
    }

    /**进度颜色, 不带透明*/
    var progressColor: Int = _color(R.color.colorAccent)

    /**步长*/
    var progressStep: Float = 5f

    private var progress: Float = 0f
    private var _recyclerView: RecyclerView? = null
    private val paint = createPaint(style = Paint.Style.FILL)
    private val drawRect = Rect()

    private fun initDrawRect(parent: RecyclerView): Boolean {
        val startChild = parent.getChildOrNull(startChildIndex) ?: return false
        val endChild = parent.getChildOrNull(endChildIndex) ?: return false

        parent.layoutManager?.let {
            val left = it.getDecoratedLeft(startChild)
            val top = it.getDecoratedTop(startChild)

            val right = it.getDecoratedRight(endChild)
            val bottom = it.getDecoratedBottom(endChild)

            drawRect.set(left, top, right, bottom)
            return true
        }
        return false
    }

    override fun onDrawOver(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDrawOver(canvas, parent, state)

        if (initDrawRect(parent)) {
            //从左到右的渐变
            drawRect.right = (drawRect.left + drawRect.width() * progress / 100).toInt()
            paint.shader = linearHorizontalGradientShader(
                drawRect.left.toFloat(), drawRect.right.toFloat(),
                intArrayOf(
                    Color.TRANSPARENT,
                    progressColor.alphaRatio(0.5f * (1f - progress / 100))
                )
            )
            canvas.drawRect(drawRect, paint)

            progress += progressStep / _refreshRateRatio
            if (progress > 150) {
                onAnimatorEnd()
            } else {
                progress = min(100f, progress)
                parent.invalidate()
            }
        }
    }

    /**移除动画*/
    private fun onAnimatorEnd() {
        _recyclerView?.removeItemDecoration(this)
    }

}