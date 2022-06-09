package com.angcyo.library.component

import com.angcyo.library.ex.hawkGet
import com.angcyo.library.ex.hawkPut
import com.orhanobut.hawk.Hawk
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/12/04
 * Copyright (c) 2020 angcyo. All rights reserved.
 */

/**自动同步保存至[Hawk]
 * [HawkListProperty]*/
class HawkProperty<T>(
    /*默认值*/
    val def: String? = null
) : ReadWriteProperty<T, String?> {

    override fun getValue(thisRef: T, property: KProperty<*>): String? =
        property.name.hawkGet(def)

    override fun setValue(thisRef: T, property: KProperty<*>, value: String?) {
        property.name.hawkPut(value)
    }
}

class HawkPropertyValue<T, Value>(
    /*默认值*/
    val def: Value
) : ReadWriteProperty<T, Value> {

    override fun getValue(thisRef: T, property: KProperty<*>): Value {
        return property.name.hawkGet<Value>(def) ?: def
    }

    override fun setValue(thisRef: T, property: KProperty<*>, value: Value) {
        property.name.hawkPut(value)
    }

}