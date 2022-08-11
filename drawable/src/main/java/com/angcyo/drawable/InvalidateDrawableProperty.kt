package com.angcyo.drawable

import android.graphics.drawable.Drawable
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * 属性改变时, 自动触发[android.graphics.drawable.Drawable.invalidateSelf]
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/08/06
 */
class InvalidateDrawableProperty<DRAWABLE : Drawable, VALUE>(var value: VALUE) :
    ReadWriteProperty<DRAWABLE, VALUE> {

    override fun getValue(thisRef: DRAWABLE, property: KProperty<*>): VALUE {
        return value
    }

    override fun setValue(thisRef: DRAWABLE, property: KProperty<*>, value: VALUE) {
        val old = this.value
        this.value = value
        if (old != value) {
            thisRef.invalidateSelf()
        }
    }

}