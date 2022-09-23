package com.angcyo.canvas.items.data

import android.graphics.Paint
import android.graphics.drawable.Drawable
import com.angcyo.canvas.Reason
import com.angcyo.canvas.Strategy
import com.angcyo.canvas.data.ItemDataBean
import com.angcyo.canvas.data.ItemDataBean.Companion.mmUnit
import com.angcyo.canvas.data.toPaintStyle
import com.angcyo.canvas.data.toPaintStyleInt
import com.angcyo.canvas.graphics.GraphicsHelper
import com.angcyo.canvas.items.BaseItem
import com.angcyo.canvas.items.renderer.BaseItemRenderer

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

    /**当渲染的bounds改变时, 是否需要刷新[updateRenderItem]*/
    open fun needUpdateOfBoundsChanged(reason: Reason): Boolean = false

    //---方法---

    /**重新更新需要渲染的界面数据*/
    fun updateRenderItem(renderer: DataItemRenderer) {
        //更新
        GraphicsHelper.updateRenderItem(renderer, dataBean)
    }

    //---操作---

    /**更新笔的样式*/
    fun updatePaintStyle(
        style: Paint.Style,
        renderer: DataItemRenderer,
        strategy: Strategy = Strategy.normal
    ) {
        val old = dataBean.paintStyle.toPaintStyle()
        val new = style
        if (old == new) {
            return
        }
        renderer.canvasView.getCanvasUndoManager().addAndRedo(strategy, {
            dataBean.paintStyle = old.toPaintStyleInt()
            updateRenderItem(renderer)
        }) {
            dataBean.paintStyle = new.toPaintStyleInt()
            updateRenderItem(renderer)
        }
    }
}