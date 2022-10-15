package com.angcyo.canvas.data

import android.graphics.Color
import android.graphics.Path
import com.angcyo.library.annotation.Pixel
import com.angcyo.library.ex.dp

/**
 * [com.angcyo.canvas.core.renderer.LimitRenderer]
 *
 * 限制框绘制的信息
 *
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/10/15
 */
data class LimitDataInfo(
    /**绘制的path, 相对于坐标系的点*/
    @Pixel
    val limitPath: Path,

    /**是否是主要的限制框
     * 用来决定[com.angcyo.canvas.core.component.InitialPointHandler]恢复显示范围
     * */
    var isPrimary: Boolean = false,

    /**是否需要绘制*/
    var enableRender: Boolean = true,

    /**需要绘制的边框宽度*/
    var limitStrokeWidth: Float = 1 * dp,
    /**需要绘制的边框颜色*/
    var limitStrokeColor: Int = Color.RED,
)