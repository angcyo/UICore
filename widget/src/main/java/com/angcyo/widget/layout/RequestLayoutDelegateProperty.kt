package com.angcyo.widget.layout

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * 委托属性, 在自定义的View中会导致xml预览失败,但是不影响apk.
 * 应该是预览界面, 使用的kotlin版本太低导致的
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020-03-07
 */
class RequestLayoutDelegateProperty<T>(var value: T) : ReadWriteProperty<LayoutDelegate, T> {
    override fun getValue(thisRef: LayoutDelegate, property: KProperty<*>): T = value

    override fun setValue(thisRef: LayoutDelegate, property: KProperty<*>, value: T) {
        this.value = value
        thisRef.delegateView.requestLayout()
    }
}