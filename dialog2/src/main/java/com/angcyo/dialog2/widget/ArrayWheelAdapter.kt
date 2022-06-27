package com.angcyo.dialog2.widget

import com.contrarywind.adapter.WheelAdapter

/**
 * The simple Array wheel adapter
 * @param <T> the element type
</T> */
class ArrayWheelAdapter<T>(val items: List<T>) : WheelAdapter<Any?> {
    override fun getItem(index: Int): Any? {
        return items.getOrNull(index)
    }

    override fun getItemsCount(): Int {
        return items.size
    }

    override fun indexOf(o: Any?): Int {
        return items.indexOf(o)
    }
}