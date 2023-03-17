package com.angcyo.canvas.render.data

import android.graphics.drawable.Drawable
import com.angcyo.canvas.render.core.CanvasRenderDelegate
import com.angcyo.library.annotation.Pixel

/**
 * 渲染参数, 各取所需
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/03/06
 */
data class RenderParams(
    /**代理*/
    var delegate: CanvasRenderDelegate? = null,
    /**需要在什么地方渲染, 标识对象
     * 比如
     * 需要绘制在[CanvasRenderDelegate]上
     * 需要绘制在[Drawable]上
     * 需要绘制在[android.graphics.Canvas]上
     * 需要绘制在[com.angcyo.dsladapter.DslAdapterItem]上
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
)
