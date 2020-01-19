package com.angcyo

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.angcyo.http.base.fromJson
import com.angcyo.http.base.toJson

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/01/18
 */

//<editor-fold desc="Bundle操作">

const val BUNDLE_KEY_JSON = "BUNDLE_KEY_JSON"

/**创建一个默认传输[json]数据的[Bundle]*/
fun jsonBundle(data: Any?, key: String = BUNDLE_KEY_JSON): Bundle {
    val bundle = Bundle()
    bundle.putData(data, key)
    return bundle
}

/**获取[Bundle]中传输的[json]数据*/
fun Bundle.getJson(key: String = BUNDLE_KEY_JSON) = getString(key)

fun Bundle.putData(data: Any?, key: String = BUNDLE_KEY_JSON): Bundle {
    putString(key, data?.covertToStr())
    return this
}

inline fun <reified DATA> Bundle.getData(key: String = BUNDLE_KEY_JSON): DATA? =
    getJson(key)?.covertFromStr()

//</editor-fold desc="Bundle操作">

fun Any.covertToStr(): String? {
    return when (this) {
        is String -> this
        is Number -> this.toString()
        else -> this.toJson()
    }
}

/**将[json]转换成对应的数据类型*/
inline fun <reified DATA> String.covertFromStr(): DATA? {
    val cls = DATA::class.java
    return when {
        cls.isAssignableFrom(String::class.java) -> this as? DATA
        cls.isAssignableFrom(Int::class.java) -> this.toInt() as? DATA
        cls.isAssignableFrom(Long::class.java) -> this.toLong() as? DATA
        cls.isAssignableFrom(Float::class.java) -> this.toFloat() as? DATA
        cls.isAssignableFrom(Double::class.java) -> this.toDouble() as? DATA
        else -> this.fromJson(cls)
    }
}

//<editor-fold desc="Fragment put get">

/**快速设置[Fragment]的参数, 如果已经存在[arguments], 则追加数据*/
fun Fragment.putData(data: Any?, key: String = BUNDLE_KEY_JSON) {
    val bundle = jsonBundle(data, key)

    arguments?.putAll(bundle)

    if (arguments == null) {
        arguments = bundle
    }
}

/**快速从[Fragment]获取[putData]设置的数据*/
inline fun <reified DATA> Fragment.getData(key: String = BUNDLE_KEY_JSON): DATA? =
    arguments?.getJson(key)?.covertFromStr()

//</editor-fold desc="Fragment put get">

//<editor-fold desc="Intent put get">

fun Intent.putData(data: Any?, key: String = BUNDLE_KEY_JSON) {
    putExtra(key, data?.covertToStr())
}

inline fun <reified DATA> Intent.getData(key: String = BUNDLE_KEY_JSON): DATA? =
    getStringExtra(key)?.covertFromStr()

//</editor-fold desc="Intent put get">

//<editor-fold desc="Activity put get">

fun Activity.putData(data: Any?, key: String = BUNDLE_KEY_JSON) {
    intent.putData(data, key)
}

inline fun <reified DATA> Activity.getData(key: String = BUNDLE_KEY_JSON): DATA? =
    intent.getData(key)

//</editor-fold desc="Activity put get">

