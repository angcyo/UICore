package com.angcyo.library.canvas.core

import android.graphics.Canvas
import com.angcyo.library.canvas.annotation.CanvasOutsideCoordinate

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/06/28
 */
interface IRenderOutside : IRender {

    /**渲染入口*/
    @CanvasOutsideCoordinate
    fun renderOnOutside(iCanvasView: ICanvasView, canvas: Canvas)

}