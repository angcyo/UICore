package com.angcyo.canvas.render.data

import android.graphics.Color
import android.graphics.Path
import android.graphics.RectF
import com.angcyo.library.annotation.Pixel
import com.angcyo.library.canvas.annotation.CanvasInsideCoordinate
import com.angcyo.library.ex.dp

/**
 * [com.angcyo.canvas.render.renderer.CanvasLimitRenderer]
 *
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/03/25
 */
data class LimitInfo(
    /**绘制的path, 相对于坐标系原点的点*/
    @Pixel
    @CanvasInsideCoordinate
    val path: Path,
    /**[path]对应的边界*/
    val bounds: RectF,
    /**线框的颜色*/
    val strokeColor: Int = Color.RED,
    /**线框的宽度*/
    val strokeWidth: Float = 1 * dp,
    /**是否激活绘制*/
    val enableRender: Boolean = true,
    /**标识位*/
    val tag: String? = null,
) {
    companion object {

        const val TAG_MAIN = "main"
    }
}