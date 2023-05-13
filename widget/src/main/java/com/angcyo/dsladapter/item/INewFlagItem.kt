package com.angcyo.dsladapter.item

import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.view.Gravity
import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.library.ex._drawable
import com.angcyo.library.ex.dpi
import com.angcyo.library.ex.hawkGetBoolean
import com.angcyo.library.ex.hawkPut
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget.R

/**
 * 用来绘制new标记的item
 * 和[com.angcyo.item.style.INewItem]的区别在于, 这个是绘制出来的
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/05/13
 */
interface INewFlagItem : IDslItem {
    /**配置类 */
    var newFlagItemConfig: NewFlagItemConfig
}

/**设置Key*/
var INewFlagItem.itemNewFlagHawkKeyStr: String?
    get() = newFlagItemConfig.itemNewFlagHawkKeyStr
    set(value) {
        newFlagItemConfig.itemNewFlagHawkKeyStr = value
    }

/**是否有new*/
var INewFlagItem.itemHaveNewFlag: Boolean
    get() = itemNewFlagHawkKeyStr.hawkGetBoolean(newFlagItemConfig.itemDefaultNewFlag && !itemNewFlagHawkKeyStr.isNullOrEmpty())
    set(value) {
        itemNewFlagHawkKeyStr.hawkPut(value)
    }

open class NewFlagItemConfig : IDslItemConfig {

    /**默认情况下, 是否有new*/
    var itemDefaultNewFlag: Boolean = true

    /**用来判断是否有new的hawk key*/
    var itemNewFlagHawkKeyStr: String? = null

    /**绘制的资源*/
    var itemNewFlagDrawable: Drawable? = _drawable(R.drawable.lib_new_svg)

    /**偏移量*/
    var itemNewFlagOffsetX: Int = 6 * dpi
    var itemNewFlagOffsetY: Int = 3 * dpi

    /**在[DslAdapterItem]的右上角绘制*/
    var itemNewFlagGravity: Int = Gravity.RIGHT or Gravity.TOP

    /**绘制标识*/
    open fun drawNewFlag(canvas: Canvas, item: DslAdapterItem, viewHolder: DslViewHolder) {
        itemNewFlagDrawable?.let {
            val itemView = viewHolder.itemView
            val offsetX = itemNewFlagOffsetX
            val offsetY = itemNewFlagOffsetY

            //调整offset
            val left = when (itemNewFlagGravity and Gravity.HORIZONTAL_GRAVITY_MASK) {
                Gravity.RIGHT -> itemView.right - it.intrinsicWidth - offsetX
                Gravity.CENTER_HORIZONTAL -> itemView.left + itemView.measuredWidth / 2 - it.intrinsicWidth / 2
                else -> itemView.left + offsetX
            }
            val top = when (itemNewFlagGravity and Gravity.VERTICAL_GRAVITY_MASK) {
                Gravity.BOTTOM -> itemView.bottom - it.intrinsicHeight - offsetY
                Gravity.CENTER_VERTICAL -> itemView.top + itemView.measuredHeight / 2 - it.intrinsicHeight / 2
                else -> itemView.top + offsetY
            }

            it.setBounds(left, top, left + it.intrinsicWidth, top + it.intrinsicHeight)
            it.draw(canvas)
        }
    }
}