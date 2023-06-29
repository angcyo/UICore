package com.angcyo.library.canvas.core

import android.graphics.Canvas
import com.angcyo.library.canvas.annotation.CanvasInsideCoordinate

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/06/28
 */
interface IRenderInside : IRender {

    /**渲染入口*/
    @CanvasInsideCoordinate
    fun renderOnInside(iCanvasView: ICanvasView, canvas: Canvas)

}