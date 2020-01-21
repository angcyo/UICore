package com.angcyo.library.ex

import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.text.SpannableStringBuilder
import android.text.TextUtils
import android.util.Base64
import android.webkit.MimeTypeMap
import androidx.annotation.ColorInt
import java.util.regex.Pattern

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/12/20
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
@ColorInt
fun String.toColorInt(): Int = Color.parseColor(this)

fun CharSequence?.or(default: CharSequence = "--") =
    if (this.isNullOrEmpty()) default else this

/**将列表连成字符串*/
fun List<Any?>.connect(
    divide: CharSequence = "," /*连接符*/,
    convert: (Any) -> CharSequence? = { it.toString() }
): CharSequence {
    return buildString {
        this@connect.forEach {
            it?.apply {
                val charSequence = convert(it)
                if (charSequence.isNullOrEmpty()) {

                } else {
                    append(charSequence).append(divide)
                }
            }
        }
        safe()
    }
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

/** 安全的去掉字符串的最后一个字符 */
fun CharSequence.safe(): CharSequence? {
    return subSequence(0, kotlin.math.max(0, length - 1))
}

fun StringBuilder.safe(): StringBuilder {
    return delete(kotlin.math.max(0, lastIndex), kotlin.math.max(0, length))
}

fun SpannableStringBuilder.safe(): SpannableStringBuilder {
    return delete(kotlin.math.max(0, lastIndex), kotlin.math.max(0, length))
}

/**判断字符串是否是纯数字*/
fun String.isNumber(): Boolean {
    if (TextUtils.isEmpty(this)) {
        return false
    }
    val pattern = Pattern.compile("^[-\\+]?[\\d]+$")
    return pattern.matcher(this).matches()
}

/**将base64字符串, 转换成图片*/
fun String.toBitmap(): Bitmap {
    val bytes = Base64.decode(this, Base64.NO_WRAP)
    return bytes.toBitmap()
}

/**url中的参数获取*/
fun String.queryParameter(key: String): String? {
    val uri = Uri.parse(this)
    return uri.getQueryParameter(key)
}

/**获取url或者文件扩展名 对应的mimeType*/
fun String.mimeType(): String? {
    return MimeTypeMap.getSingleton()
        .getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(this))
}

/**获取字符串中所有匹配的数据(部分匹配), 更像是contains的关系*/
fun CharSequence?.patternList(regex: String?): MutableList<String> {
    val result = mutableListOf<String>()
    if (this == null) {
        return result
    }
    regex?.let {
        val matcher = regex.toPattern().matcher(this)
        while (matcher.find()) {
            result.add(matcher.group())
        }
    }
    return result
}

/**是否匹配成功(完成匹配)*/
fun CharSequence?.pattern(regex: String?, allowEmpty: Boolean = true): Boolean {

    if (TextUtils.isEmpty(this)) {
        if (allowEmpty) {
            return true
        }
    }

    if (this == null) {
        return false
    }
    if (regex == null) {
        if (allowEmpty) {
            return true
        }
        return !TextUtils.isEmpty(this)
    }
    val matcher = regex.toPattern().matcher(this)
    return matcher.matches()
}

/**列表中的所有正则都匹配*/
fun CharSequence?.pattern(regexList: Iterable<String>, allowEmpty: Boolean = true): Boolean {
    if (TextUtils.isEmpty(this)) {
        if (allowEmpty) {
            return true
        }
    }

    if (this == null) {
        return false
    }

    if (!regexList.iterator().hasNext()) {
        if (allowEmpty) {
            return true
        }
        return !TextUtils.isEmpty(this)
    }

    var result = false

    // 有BUG的代码, if 条件永远不会为true, 虽然你已经给 result 赋值了true
    //    regexList.forEach {
    //        result = this.pattern(it, allowEmpty)
    //        if (result) {
    //            return@forEach
    //        }
    //    }

    regexList.forEach {
        //闭包外面声明的变量, 虽然已经修改了, 但是修改后的值, 不影响 if 条件判断
        if (this.pattern(it, allowEmpty)) {
            result = true
            return@forEach
        }
    }

    return result
}
