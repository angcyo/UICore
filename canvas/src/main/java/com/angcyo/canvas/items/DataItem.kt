package com.angcyo.canvas.items

import android.graphics.drawable.Drawable
import com.angcyo.canvas.data.ItemDataBean
import com.angcyo.canvas.data.ItemDataBean.Companion.mmUnit
import com.angcyo.canvas.graphics.GraphicsHelper
import com.angcyo.canvas.items.renderer.BaseItemRenderer
import com.angcyo.canvas.items.renderer.DataItemRenderer

/**
 * [com.angcyo.canvas.data.ItemDataBean]
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/09/21
 */
open class DataItem(val dataBean: ItemDataBean) : BaseItem() {

    /**
     * 通过改变此对象, 呈现出不同的可视图画
     * 可绘制的对象*/
    var drawable: Drawable? = null

    override fun getItemScaleX(renderer: BaseItemRenderer<*>): Float {
        val width = mmUnit.convertValueToPixel(dataBean.width)
        return renderer.getBounds().width() / width
    }

    override fun getItemScaleY(renderer: BaseItemRenderer<*>): Float {
        val height = mmUnit.convertValueToPixel(dataBean.height)
        return renderer.getBounds().height() / height
    }

    //

    /**重新更新需要渲染的界面数据*/
    fun updateRenderItem(renderer: DataItemRenderer) {
        //更新
        GraphicsHelper.updateRenderItem(renderer, dataBean)
    }

}