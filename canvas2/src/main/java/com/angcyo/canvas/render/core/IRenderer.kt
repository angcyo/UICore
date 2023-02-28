package com.angcyo.canvas.render.core

import android.graphics.Canvas

/**
 * 声明一个可以渲染的组件
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/02/14
 */
interface IRenderer {

    /**绘制入口*/
    fun render(canvas: Canvas)

}