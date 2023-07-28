package com.angcyo.library.component.hawk

import androidx.annotation.Keep
import com.angcyo.library.ex.hawkGet
import com.angcyo.library.ex.hawkPut
import com.angcyo.library.getAppVersionCode
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

/**当有Hawk属性赋值时回调的方法*/
typealias HawkPropertyChangeAction = (key: String) -> Unit

/**自动同步保存至[Hawk]
 * [com.angcyo.library.component.hawk.HawkListProperty]*/
@Keep
class HawkProperty<T>(
    /*默认值*/
    val def: String? = null
) : ReadWriteProperty<T, String?> {

    companion object {
        /**全局的Hawk属性变化回调*/
        val hawkPropertyChangeActionList = mutableListOf<HawkPropertyChangeAction>()
    }

    override fun getValue(thisRef: T, property: KProperty<*>): String? =
        property.name.hawkGet(def)

    override fun setValue(thisRef: T, property: KProperty<*>, value: String?) {
        property.name.hawkPut(value)
        hawkPropertyChangeActionList.forEach {
            it(property.name)
        }
    }
}

/** var Z_MODEL: Int by HawkPropertyValue<Any, Int>(-1) */
@Keep
class HawkPropertyValue<T, Value>(
    /*默认值*/
    val def: Value,
    /*更新值的回调*/
    val onSetValue: (newValue: Value) -> Unit = {}
) : ReadWriteProperty<T, Value> {

    override fun getValue(thisRef: T, property: KProperty<*>): Value {
        return property.name.hawkGet<Value>(def) ?: def
    }

    override fun setValue(thisRef: T, property: KProperty<*>, value: Value) {
        property.name.hawkPut(value)
        onSetValue(value)
        HawkProperty.hawkPropertyChangeActionList.forEach {
            it(property.name)
        }
    }
}

/**指定后缀[suffix], 自动保存[Hawk]
 * [HawkPropertyValue]*/
@Keep
class HawkPropertySuffixValue<T, Value>(
    /*后缀*/
    val suffix: String,
    /*默认值*/
    val def: Value,
    /*更新值的回调*/
    val onSetValue: (key: String, newValue: Value) -> Unit = { _, _ -> }
) : ReadWriteProperty<T, Value> {

    private val KProperty<*>.hawkKey: String
        get() = "${name}_${suffix}"

    override fun getValue(thisRef: T, property: KProperty<*>): Value {
        return property.hawkKey.hawkGet<Value>(def) ?: def
    }

    override fun setValue(thisRef: T, property: KProperty<*>, value: Value) {
        val key = property.hawkKey
        key.hawkPut(value)
        onSetValue(key, value)
        HawkProperty.hawkPropertyChangeActionList.forEach {
            it(property.name)
        }
    }
}

/**跟随当前App版本号, 自动保存[Hawk]
 * [HawkPropertyValue]*/
@Keep
class HawkPropertyVersionValue<T, Value>(
    /*默认值*/
    val def: Value,
    /*更新值的回调*/
    val onSetValue: (key: String, newValue: Value) -> Unit = { _, _ -> }
) : ReadWriteProperty<T, Value> {

    private val KProperty<*>.hawkKey: String
        get() = "${name}_${getAppVersionCode()}"

    override fun getValue(thisRef: T, property: KProperty<*>): Value {
        return property.hawkKey.hawkGet<Value>(def) ?: def
    }

    override fun setValue(thisRef: T, property: KProperty<*>, value: Value) {
        val key = property.hawkKey
        key.hawkPut(value)
        onSetValue(key, value)
        HawkProperty.hawkPropertyChangeActionList.forEach {
            it(property.name)
        }
    }
}