package com.angcyo.canvas.graphics

import android.graphics.Bitmap
import com.angcyo.canvas.CanvasDelegate
import com.angcyo.canvas.data.ItemDataBean
import com.angcyo.canvas.data.ItemDataBean.Companion.mmUnit
import com.angcyo.canvas.items.renderer.DataItemRenderer
import com.angcyo.canvas.utils.CanvasConstant
import com.angcyo.library.ex.toBase64Data
import com.angcyo.library.unit.MmValueUnit

/**
 * 扩展方法
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/09/21
 */

/**添加一个[bitmap]数据渲染*/
fun CanvasDelegate.addBitmapRender(bitmap: Bitmap?): DataItemRenderer? {
    bitmap ?: return null
    val bean = ItemDataBean()
    bean.mtype = CanvasConstant.DATA_TYPE_BITMAP
    bean.imageOriginal = bitmap.toBase64Data()

    bean.width = mmUnit.convertPixelToValue(bitmap.width.toFloat())
    bean.height = mmUnit.convertPixelToValue(bitmap.height.toFloat())

    return GraphicsHelper.renderItemData(this, bean)
}