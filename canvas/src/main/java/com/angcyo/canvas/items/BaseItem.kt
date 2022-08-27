package com.angcyo.canvas.items

import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.drawable.Drawable
import com.angcyo.canvas.items.renderer.BaseItemRenderer
import com.angcyo.canvas.utils.CanvasConstant
import com.angcyo.library.ex.uuid

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/08
 */
abstract class BaseItem : ICanvasItem {

    /**唯一标识符*/
    var uuid: String = uuid()

    /**雕刻数据的索引, 改变位置后, 不用更新索引. 调整宽高旋转分辨率后需要更新索引*/
    var engraveIndex: Int? = null

    /**数据类型
     * [CanvasConstant.DATA_TYPE_BITMAP]
     * [CanvasConstant.DATA_TYPE_TEXT]
     * [CanvasConstant.DATA_TYPE_SVG]
     * [CanvasConstant.DATA_TYPE_GCODE]
     * */
    var dataType: Int = 0

    /**数据处理的模式
     * [CanvasConstant.DATA_MODE_GCODE]
     * [CanvasConstant.DATA_MODE_DITHERING]
     * [CanvasConstant.DATA_MODE_BLACK_WHITE]
     * */
    var dataMode: Int = 0

    /**用来存放自定义的数据*/
    var data: Any? = null

    //

    /**自身实际的宽, 用来计算最终的缩放比*/
    var itemWidth: Float = 0f

    /**自身实际的高, 用来计算最终的缩放比*/
    var itemHeight: Float = 0f

    //

    /**图层预览的名称*/
    override var itemLayerName: CharSequence? = null

    /**图层预览的图形*/
    override var itemLayerDrawable: Drawable? = null

    // ---

    /**更新[BaseItem]用来重新绘制内容
     * [com.angcyo.canvas.items.renderer.BaseItemRenderer.requestRendererItemUpdate]
     * */
    open fun updateItem(paint: Paint) {

    }

    // ---

    /**当前x的缩放比*/
    open fun getItemScaleX(renderer: BaseItemRenderer<*>): Float {
        return renderer.getBounds().width() / itemWidth
    }

    open fun getItemScaleY(renderer: BaseItemRenderer<*>): Float {
        return renderer.getBounds().height() / itemHeight
    }

    /**获取数据变换的矩阵*/
    open fun getMatrix(renderer: BaseItemRenderer<*>): Matrix {
        val matrix = Matrix()
        val bounds = renderer.getBounds()
        //缩放到指定大小
        matrix.setScale(bounds.width() / itemWidth, bounds.height() / itemHeight, 0f, 0f)
        //旋转到指定角度
        matrix.postRotate(renderer.rotate, bounds.width() / 2f, bounds.height() / 2f)
        //平移到指定位置
        matrix.postTranslate(bounds.left, bounds.top)
        return matrix
    }
}