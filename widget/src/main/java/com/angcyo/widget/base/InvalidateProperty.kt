package com.angcyo.widget.base

import android.view.View
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/12/24
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
class InvalidateProperty<T>(var value: T) : ReadWriteProperty<View, T> {
    override fun getValue(thisRef: View, property: KProperty<*>): T = value

    override fun setValue(thisRef: View, property: KProperty<*>, value: T) {
        this.value = value
        thisRef.postInvalidate()
    }
}