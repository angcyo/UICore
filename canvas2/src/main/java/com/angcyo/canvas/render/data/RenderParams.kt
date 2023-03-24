package com.angcyo.canvas.render.data

import android.graphics.Paint
import android.graphics.drawable.Drawable
import com.angcyo.canvas.render.core.CanvasRenderDelegate
import com.angcyo.library.annotation.Pixel

/**
 * 渲染参数, 各取所需
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/03/06
 */
data class RenderParams(

    /**代理, 用来获取*/
    var delegate: CanvasRenderDelegate? = null,

    /**需要在什么地方渲染, 标识对象
     * 比如
     * 需要绘制在[CanvasRenderDelegate]上
     * 需要绘制在[Drawable]上
     * 需要绘制在[android.graphics.Canvas]上
     * 需要绘制在[com.angcyo.dsladapter.DslAdapterItem]上
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
    var drawMinHeight: Float = 1f
) {

    /**绘制[android.graphics.Path]需要控制的画笔宽度*/
    fun updateDrawPathPaintStrokeWidth(paint: Paint) {
        val renderDst = renderDst
        if (renderDst is CanvasRenderDelegate) {
            val scale = renderDst.renderViewBox.getScale()
            paint.strokeWidth = paint.strokeWidth / scale //确保边框线的可见性
        } else if (renderDst is Float) {
            // renderDst = 需要绘制的目标大小/目标真实大小
            paint.strokeWidth = paint.strokeWidth / renderDst
        }
    }

}
