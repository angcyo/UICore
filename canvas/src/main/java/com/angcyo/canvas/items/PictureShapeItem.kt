package com.angcyo.canvas.items

import android.graphics.Paint
import android.graphics.Path
import com.angcyo.canvas.utils.CanvasConstant
import com.angcyo.library.component.pool.acquireTempRectF
import com.angcyo.library.component.pool.release
import com.angcyo.library.ex.*
import kotlin.math.max

/**
 * 形状组件渲染数据
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/29
 */
class PictureShapeItem(
    /**需要绘制的形状[Path]数据*/
    val shapePath: Path
) : PictureDrawableItem() {

    companion object {
        /**默认的形状宽度, cm单位*/
        const val SHAPE_DEFAULT_WIDTH = 10f

        /**默认的形状高度, cm单位*/
        const val SHAPE_DEFAULT_HEIGHT = 10f
    }

    init {
        itemLayerName = "Shape"
        dataType = CanvasConstant.DATA_TYPE_PATH
        dataMode = CanvasConstant.DATA_MODE_GCODE
    }

    /**将[shapePath]转换成可以渲染的[Drawable]*/
    override fun updateItem(paint: Paint) {
        val path = shapePath
        val bounds = acquireTempRectF()
        path.computeBounds(bounds, true)

        val shapeWidth = max(1f, bounds.width())
        val shapeHeight = max(1f, bounds.height())

        drawable = createPathDrawable(path, shapeWidth, shapeHeight, paint)
        itemWidth = shapeWidth
        itemHeight = shapeHeight

        bounds.release()
    }

    override fun updateDrawable(paint: Paint, width: Float, height: Float) {
        drawable = createPathDrawable(shapePath, width, height, paint)
    }

}