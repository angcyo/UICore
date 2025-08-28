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
    val bounds: RectF? = null,
    /**线框的颜色*/
    val strokeColor: Int? = Color.RED,
    /**线框的宽度*/
    val strokeWidth: Float = 1 * dp,
    /**是否激活绘制*/
    var enableRender: Boolean = true,
    /**标识位*/
    val tag: String? = null,
    //--
    /**是否填充*/
    val isFill: Boolean = false,
    val fillColor: Int? = 0x0f000000,
) {
    companion object {

        /**主要的限制*/
        const val TAG_MAIN = "main"

        /**灰度区域*/
        const val TAG_GRAY = "gray"
    }
}