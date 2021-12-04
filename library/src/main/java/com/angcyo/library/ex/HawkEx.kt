package com.angcyo.library.ex

import android.text.TextUtils
import com.orhanobut.hawk.Hawk

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/06/11
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */

const val HAWK_SPLIT_CHAR = ","

/**
 * this 对应的 key值,
 * 将获取到的 value 用 `,` 分割, 返回列表集合 (不含空字符)
 * @param maxCount 需要返回多少条
 * */
fun String?.hawkGetList(def: String? = "", maxCount: Int = Int.MAX_VALUE): List<String> {
    val result = mutableListOf<String>()
    this?.let {
        val get = Hawk.get(it, def)

        if (!TextUtils.isEmpty(get)) {
            result.addAll(get.splitList(HAWK_SPLIT_CHAR, false, true, maxCount))
        }
    }
    return result
}

/**
 * 将 value, 追加到 原来的value中
 *
 * @param sort 默认排序, 最后追加的在首位显示
 *
 * */
fun String?.hawkPutList(
    value: String?,
    sort: Boolean = true,
    allowEmpty: Boolean = false
): Boolean {
    if (value.isNullOrEmpty()) {
        if (!allowEmpty) {
            return false
        }
    }
    val char = HAWK_SPLIT_CHAR
    return this?.run {

        val oldString = Hawk.get(this, "")
        val oldList = oldString.splitList(char)

        if (value.isNullOrBlank() && allowEmpty) {
            return Hawk.put(this, "")
        }

        if (!sort) {
            if (oldList.contains(value)) {
                //已存在
                return true
            }
        }

        oldList.remove(value)
        /*最新的在前面*/
        oldList.add(0, value!!)
        Hawk.put(this, "${oldList.connect(char)}")
    } ?: false
}

fun String?.hawkPutList(
    value: List<String>?,
    sort: Boolean = true,
    allowEmpty: Boolean = false
): Boolean {
    var result = false
    value?.forEach {
        result = this?.hawkPutList(it, sort, allowEmpty) == true || result
    }
    return result
}

fun String?.hawkPut(value: CharSequence?): Boolean {
    return this?.run {
        Hawk.put(this, value)
    } ?: false
}

/**数字累加计算,保存并返回*/
fun String?.hawkAccumulate(value: Long = 1, def: Long = 0): Long {
    val origin = hawkGet(null)?.toLongOrNull() ?: def
    val newValue = origin + value
    hawkPut("$newValue")
    return newValue
}

/**直接追加到原来的数据尾部*/
fun String?.hawkAppend(value: CharSequence?, symbol: String = ""): Boolean {
    return this?.run {
        Hawk.put(this, "${hawkGet() ?: ""}${symbol}${value ?: ""}")
    } ?: false
}

fun String?.hawkGet(def: String? = null): String? {
    var result: String? = null
    this?.let {
        result = Hawk.get<String>(it, def)
    }
    return result
}

fun String?.hawkDelete(): Boolean {
    return this?.run {
        Hawk.delete(this)
    } ?: false
}

