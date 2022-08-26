package com.angcyo.canvas.items

import android.graphics.Paint
import android.graphics.drawable.Drawable
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

    /**额外存储的数据, 支持回退栈管理*/
    var holdData: Map<String, Any?>? = null

    //

    /**自身实际的宽*/
    var itemWidth: Float = 0f

    /**自身实际的高*/
    var itemHeight: Float = 0f

    //

    /**图层预览的名称*/
    override var itemLayerName: CharSequence? = null

    /**图层预览的图形*/
    override var itemLayerDrawable: Drawable? = null

    //

    /**更新[BaseItem]用来重新绘制内容*/
    open fun updateItem(paint: Paint) {

    }
}

/**获取额外存储的数据
 * [com.angcyo.canvas.items.BaseItem.holdData]*/
inline fun <reified T> BaseItem.getHoldData(key: String): T? {
    val any = holdData?.get(key)
    if (any is T) {
        return any
    }
    return null
}

/**设置额外的数据存储*/
fun <T> BaseItem.setHoldData(key: String, value: T?) {
    if (holdData == null) {
        holdData = hashMapOf()
    }
    var data = holdData
    if (data is MutableMap<String, Any?>) {
        //no
    } else {
        holdData = hashMapOf<String, Any?>().apply {
            putAll(data!!)
        }
        data = holdData
    }

    if (data is MutableMap<String, Any?>) {
        data.put(key, value)
    }
}