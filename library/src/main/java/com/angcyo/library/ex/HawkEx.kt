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

/**
 * this 对应的 key值,
 * 将获取到的 value 用 `,` 分割, 返回列表集合 (不含空字符)
 * @param maxCount 需要返回多少条
 * */
fun String?.hawkGetList(maxCount: Int = Int.MAX_VALUE): MutableList<String> {
    val result = mutableListOf<String>()
    this?.let {
        val get = Hawk.get(it, "")

        if (!TextUtils.isEmpty(get)) {
            result.addAll(get.split(",", false, true, maxCount))
        }

    }
    return result
}

/**分割字符串*/
fun String?.split(
    separator: String = ",",
    allowEmpty: Boolean = false,
    checkExist: Boolean = false,
    maxCount: Int = Int.MAX_VALUE
): List<String> {
    val result = mutableListOf<String>()

    if (this.isNullOrEmpty()) {
    } else if (this.toLowerCase() == "null") {
    } else if (separator.isNullOrEmpty()) {
    } else {
        for (s in this.split(separator.toRegex(), Int.MAX_VALUE)) {
            if (s.isNullOrEmpty() && !allowEmpty) {
                continue
            }
            if (result.contains(s) && checkExist) {
                continue
            }

            result.add(s)

            if (result.size >= maxCount) {
                break
            }
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
fun String?.hawkPutList(value: String?, sort: Boolean = true) {
    if (TextUtils.isEmpty(value)) {
        return
    }
    this?.let {

        val oldString = Hawk.get(it, "")
        if (!sort) {
            if (oldString?.contains("$value") == true) {
                return@let
            }
        }

        Hawk.put(
            it,
            //最新的在前面
            "$value," + oldString
                .replace(",,", ",")
                .replace(value ?: " ", "")
        )
    }
}

fun String?.hawkPut(value: CharSequence?) {
    if (TextUtils.isEmpty(value)) {
        return
    }
    this?.let {
        Hawk.put(it, value ?: "")
    }
}

fun String?.hawkAppend(value: CharSequence?) {
    if (TextUtils.isEmpty(value)) {
        return
    }
    this?.let {
        Hawk.put(it, "${hawkGet() ?: ""}${value ?: ""}")
    }
}

fun String?.hawkGet(): String? {
    var result: String? = null
    this?.let {
        result = Hawk.get<String>(it, null)
    }
    return result
}

fun String?.hawkDelete() {
    this?.let {
        Hawk.delete(it)
    }
}