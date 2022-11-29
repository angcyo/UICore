package com.angcyo.canvas.core.renderer

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import com.angcyo.canvas.core.ICanvasView
import com.angcyo.canvas.core.RenderParams
import com.angcyo.canvas.data.LimitDataInfo
import com.angcyo.canvas.utils.createPaint
import com.angcyo.library.annotation.Private
import com.angcyo.library.ex.computePathBounds

/**
 * 打印限制提示框渲染
 *
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/24
 */
class LimitRenderer(canvasView: ICanvasView) : BaseRenderer(canvasView) {

    /**画笔*/
    val paint = createPaint().apply {
        color = Color.RED
        style = Paint.Style.STROKE
    }

    /**需要绘制的数据列表*/
    val limitList = mutableListOf<LimitDataInfo>()

    //坐标系统中的坐标
    @Private
    val _limitPathBounds: RectF = RectF()

    override fun render(canvas: Canvas, renderParams: RenderParams) {
        limitList.forEach {
            if (it.enableRender) {
                drawLimitData(canvas, it)
            }
        }
    }

    @Private
    fun drawLimitData(canvas: Canvas, pathInfo: LimitDataInfo) {
        val path = pathInfo.limitPath
        if (!path.isEmpty) {
            path.computeBounds(_limitPathBounds, true)
            val scale = canvasViewBox.getScaleX()
            paint.color = pathInfo.limitStrokeColor
            paint.strokeWidth = pathInfo.limitStrokeWidth / scale //抵消坐标系的缩放
            canvas.drawPath(path, paint)
        }
    }

    /**获取主要的限制Bounds*/
    fun getPrimaryLimitBounds(): RectF? {
        val primaryInfo = limitList.find { it.isPrimary }
        return primaryInfo?.run {
            limitPath.computePathBounds()
        }
    }

    /**获取主要的限制信息*/
    fun getPrimaryLimitInfo(): LimitDataInfo? = limitList.find { it.isPrimary }

    /**重置所有渲染数据*/
    fun resetLimit(block: MutableList<LimitDataInfo>.() -> Unit) {
        clear()
        limitList.block()
        refresh()
    }

    /**添加一个渲染数据*/
    fun addLimit(block: MutableList<LimitDataInfo>.() -> Unit) {
        limitList.block()
        refresh()
    }

    /**清除限制框*/
    fun clear() {
        limitList.clear()
        refresh()
    }
}