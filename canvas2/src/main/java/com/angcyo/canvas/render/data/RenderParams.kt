package com.angcyo.canvas.render.data

import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.drawable.Drawable
import com.angcyo.canvas.render.core.CanvasRenderDelegate
import com.angcyo.canvas.render.data.RenderParams.Companion.RASTERIZE_HOST
import com.angcyo.library.annotation.Pixel

/**
 * 渲染参数, 各取所需
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/03/06
 */
data class RenderParams(

    /**代理, 用来获取*/
    var delegate: CanvasRenderDelegate? = null,

    /**渲染宿主, 比如在画布上渲染/在栅格化上渲染等
     * [RASTERIZE_HOST]*/
    var renderHost: Any? = null,

    /**需要在什么地方渲染, 标识对象
     * 比如
     * 需要绘制在[CanvasRenderDelegate]上
     * 需要绘制在[Drawable]上
     * 需要绘制在[android.graphics.Canvas]上
     * 需要绘制在[com.angcyo.dsladapter.DslAdapterItem]上
     * 目标需要缩放绘制, 则画笔会相应的方向放大,=[需要绘制的目标大小/目标真实大小]
     *
     * [updateDrawPathPaintStrokeWidth]
     * */
    var renderDst: Any? = null,

    /**需要覆盖的输出宽高, 手动处理参数*/
    @Pixel
    var overrideSize: Float? = null,
    /**绘制的最小宽高*/
    @Pixel
    var drawMinWidth: Float = 1f,
    @Pixel
    var drawMinHeight: Float = 1f,

    /**绘制的宽高偏移*/
    @Pixel
    var drawOffsetWidth: Float = 0f,
    @Pixel
    var drawOffsetHeight: Float = 0f,

    /**[overrideSize]如果大于目标尺寸时, 是否需要阻止放大*/
    var overrideSizeNotZoomIn: Boolean = false,

    /**渲染图片, path时的矩阵, 不指定则会从[com.angcyo.canvas.render.core.component.CanvasRenderProperty]中获取*/
    var renderMatrix: Matrix? = null
) {

    companion object {
        const val RASTERIZE_HOST = "rasterize_host"
        /*fun overrideSize(overrideSize: Float): RenderParams {
            val params = RenderParams()
            params.renderDst = overrideSize / rect.width()
            return params
        }*/
    }

    /**绘制[android.graphics.Path]需要控制的画笔宽度
     * [refStrokeWidth] 参考的画笔宽度, 不指定则使用[Paint.strokeWidth]
     * @return 返回作用的生效倍数*/
    fun updateDrawPathPaintStrokeWidth(paint: Paint, refStrokeWidth: Float? = null): Float {
        val renderDst = renderDst
        if (renderDst is CanvasRenderDelegate) {
            val scale = renderDst.renderViewBox.getScale()
            paint.strokeWidth = (refStrokeWidth ?: paint.strokeWidth) / scale //确保边框线的可见性
            return scale
        } else if (renderDst is Float) {
            // renderDst = 需要绘制的目标大小/目标真实大小
            paint.strokeWidth = (refStrokeWidth ?: paint.strokeWidth) / renderDst
            return renderDst
        }
        return 1f
    }
}

