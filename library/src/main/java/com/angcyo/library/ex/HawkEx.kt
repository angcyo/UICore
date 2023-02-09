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

const val HAWK_SPLIT_CHAR = "|"

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
fun String?.hawkPutList(value: CharSequence?, sort: Boolean = true): Boolean {
    if (value.isNullOrEmpty()) {
        return false
    }
    val char = HAWK_SPLIT_CHAR
    return this?.run {
        val oldString = Hawk.get(this, "")
        val oldList = oldString.splitList(char)

        if (!sort) {
            if (oldList.contains(value)) {
                //已存在
                return true
            }
        }

        oldList.remove(value)
        /*最新的在前面*/
        oldList.add(0, value.toString())
        Hawk.put(this, oldList.connect(char))
    } ?: false
}

/**直接替换整个[value]*/
fun String?.hawkPutList(
    value: List<CharSequence>?,
    sort: Boolean = true,
    allowEmpty: Boolean = true
): Boolean {
    this ?: return false
    var result = false
    Hawk.put(this, "")//先清空
    if (value.isNullOrEmpty() && allowEmpty) {
        result = true
    } else {
        value?.forEach {
            //挨个追加
            result = hawkPutList(it, sort) == true || result
        }
    }
    return result
}

fun String?.hawkPut(value: CharSequence?): Boolean {
    return this?.run {
        Hawk.put(this, value)
    } ?: false
}

/**
 *  var cacheLoginBean: LoginBean?
 *    get() = KEY_LOGIN_BEAN.hawkGet<LoginBean>()
 *    set(value) {
 *      KEY_LOGIN_BEAN.hawkPut(value)
 *   }
 * */
fun <T> String?.hawkPut(value: T?): Boolean {
    return this?.run {
        Hawk.put(this, value)
    } ?: false
}

/**如果key对应的值不存在或者为null时, 则设置值*/
fun <T> String?.hawkPutIfNull(value: T?): Boolean {
    return this?.run {
        val key = this
        val old: T? = Hawk.get(key)
        if (old == null) {
            Hawk.put(key, value)
        }
        true
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
        try {
            result = Hawk.get<String>(it, def)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    return result
}

/**
 * KEY_LOGIN_BEAN.hawkGet<LoginBean>()
 * */
fun <T> String?.hawkGet(def: T? = null): T? {
    var result: T? = def
    this?.let {
        try {
            result = Hawk.get<T>(it, def)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    return result
}

fun String?.hawkGetBoolean(def: Boolean = false): Boolean {
    var result: Boolean = def
    this?.let {
        try {
            result = Hawk.get(it, def)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    return result
}

fun String?.hawkGetInt(def: Int = -1): Int {
    var result = def
    this?.let {
        try {
            result = Hawk.get(it, def)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    return result
}

fun String?.hawkGetFloat(def: Float = -1f): Float {
    var result = def
    this?.let {
        try {
            result = Hawk.get(it, def)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    return result
}

fun String?.hawkDelete(): Boolean {
    return this?.run {
        Hawk.delete(this)
    } ?: false
}

