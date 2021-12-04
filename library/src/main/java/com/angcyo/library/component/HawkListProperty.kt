package com.angcyo.library.component

import com.angcyo.library.ex.hawkGetList
import com.angcyo.library.ex.hawkPutList
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
 * [HawkProperty]*/
class HawkListProperty<T>(
    val def: String?,
    /*是否重新排序*/
    val sort: Boolean = true,
    /*是否允许空值*/
    val allowEmpty: Boolean = false,
    /*最大获取数量*/
    val maxCount: Int = Int.MAX_VALUE
) : ReadWriteProperty<T, List<String>> {

    override fun getValue(thisRef: T, property: KProperty<*>): List<String> =
        property.name.hawkGetList(def, maxCount)

    override fun setValue(thisRef: T, property: KProperty<*>, value: List<String>) {
        val key = property.name
        if (value.isNullOrEmpty() && allowEmpty) {
            Hawk.put(key, "")
        } else if (value.isNotEmpty()) {
            key.hawkPutList(value, sort, allowEmpty)
        }
    }
}