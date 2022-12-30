package com.angcyo.dialog2.widget

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import com.contrarywind.adapter.IWheelDraw
import com.contrarywind.adapter.WheelAdapter
import com.contrarywind.view.WheelView

/**
 * The simple Array wheel adapter
 * @param <T> the element type
</T> */
open class ArrayWheelAdapter<T>(val items: List<T>) : WheelAdapter<Any?>, IWheelDraw {
    override fun getItem(index: Int): Any? {
        return items.getOrNull(index)
    }

    override fun getItemsCount(): Int {
        return items.size
    }

    override fun indexOf(o: Any?): Int {
        return items.indexOf(o)
    }

    override fun onDrawOnText(
        wheelView: WheelView,
        canvas: Canvas,
        text: String?,
        textDrawX: Float,
        textDrawY: Float,
        textDrawPaint: Paint,
        index: Int,
        textBounds: Rect
    ) {

    }
}