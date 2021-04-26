package com.angcyo.acc2.parse

import android.graphics.PointF
import android.graphics.Rect
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.angcyo.acc2.action.Action
import com.angcyo.acc2.core.AccNodeLog
import com.angcyo.library._screenHeight
import com.angcyo.library._screenWidth
import com.angcyo.library.ex.*
import com.angcyo.library.utils.getFloatNum
import java.net.URLEncoder

/**
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/12/31
 */

/**从字符串中, 获取所有正负整数*/
fun String?.getIntList(): List<Int> {
    val regex = "[-]?\\d+".toRegex()
    return patternList(regex.toPattern()).mapTo(mutableListOf()) { it.toIntOrNull() ?: 0 }
}

//扩展函数

/**判断是否有指定的参数信息
 * [pass]
 * [pass:]
 * [sta:xx pass:xxx]*/
fun String.haveArg(key: String): Boolean {
    var have = false
    val pattern = "($key)|($key:).*".toPattern()

    for (text in split(" ")) {
        if (text == key || pattern.matcher(text).matches()) {
            have = true
            break
        }
    }
    return have
}

/**
 * 从字符串中获取模版参数 key:value
 * 特殊字符请使用[URLEncoder.encode]加码后的字符
 * [str] 数据字符串 "code:$[ke+y+] k:key type:30400 \b last line"
 * [key] 键值 code 或k 或type
 */
fun String.arg(key: String, delimiters: Char = ':') =
    patternList("(?<=$key$delimiters)(\\S+)").firstOrNull()

/**将 input:$[abc] clear:true 转换成 key value的形式*/
fun String.toKeyValue(delimiters: Char = ':'): List<Pair<String, String?>> {
    val result = mutableListOf<Pair<String, String?>>()
    val keyPattern = "(\\S+)(?=$delimiters)".toPattern()
    val valuePattern = "(?<=$delimiters)(\\S*)".toPattern()
    val keyList = patternList(keyPattern)
    val valueList = patternList(valuePattern)
    keyList.forEachIndexed { index, key ->
        result.add(key to valueList.getOrNull(index))
    }
    return result
}


/**字符串中, 是否包含分隔符 ~*/
fun String?.havePartition(text: CharSequence = Action.POINT_SPLIT) = have(text)

/**修正索引, 比如是负数的索引*/
fun Int.revise(size: Int) = if (this < 0) this + size else this
fun Long.revise(size: Int) = if (this < 0) this + size else this

/**列表中的每一项, 都需要匹配返回一个非空的集合, 最终才会合并返回所有的集合*/
fun <T, R> List<T>.eachMatchItem(action: (index: Int, item: T) -> List<R>): List<R> {
    val result = mutableListOf<R>()
    var fail = false
    var index = -1
    for (item in this) {
        index++
        val list = action(index, item)
        if (list.isEmpty()) {
            fail = true
            break
        }
        result.addAll(list)
    }
    return if (fail) emptyList() else result
}

/**根据[indexString], 自适应是否取范围, 还是取单个
 * 支持格式[0 1 2 -2 -3] [0~-2]*/
fun <T> List<T>.eachRangeItem(
    indexString: String?,
    action: (item: T, isIn: Boolean) -> Unit = { _, _ -> }
): List<T> {
    val result = mutableListOf<T>()

    //空字符, 表示所有
    if (indexString.isNullOrEmpty()) {
        forEachIndexed { index, item ->
            result.add(item)
            action(item, true)
        }
        return result
    }

    //随机获取
    if (indexString.contains(Action.RANDOM)) {
        val count = indexString.arg(Action.RANDOM)?.toIntOrNull() ?: 1
        val randomGet = randomGet(count)
        forEachIndexed { index, item ->
            if (randomGet.contains(item)) {
                result.add(item)
                action(item, true)
            } else {
                action(item, false)
            }
        }
        return result
    }

    val intList = indexString.getIntList().mapTo(mutableListOf()) { it.revise(size) }
    if (indexString.havePartition()) {
        if (intList.size >= 2) {
            //有分隔符 0~-2:取从0到倒数第二个
            val startIndex = intList[0]
            val endIndex = intList[1]

            forEachIndexed { index, item ->
                if (index in startIndex..endIndex) {
                    //do it
                    result.add(item)
                    action(item, true)
                } else {
                    action(item, false)
                }
            }
            return result
        }
    }
    //如果没有分隔符 1 2 3:取第1个 第2个 第3个
    forEachIndexed { index, item ->
        if (index in intList) {
            //do it
            result.add(item)
            action(item, true)
        } else {
            action(item, false)
        }
    }
    return result
}

/**分割参数*/
fun String?.args(
    delimiters: String = ":",
    action: (index: Int, arg: String) -> Unit = { _, _ -> }
): List<String> {
    return this?.split(delimiters)?.apply {
        forEachIndexed(action)
    } ?: emptyList()
}

fun AccessibilityNodeInfoCompat.toLog(logNodeChild: Boolean = false): StringBuilder {
    val accNodeLog = AccNodeLog()
    accNodeLog.logNodeChild = logNodeChild
    return accNodeLog.getNodeLog(this)
}

/**转成日志*/
fun List<AccessibilityNodeInfoCompat>.toLog(
    header: String? = null,
    logNodeChild: Boolean = false
) = buildString {
    if (header != null) {
        appendLine(header)
    }
    val accNodeLog = AccNodeLog()
    accNodeLog.logNodeChild = logNodeChild

    val list = this@toLog
    list.forEachIndexed { index: Int, node: AccessibilityNodeInfoCompat ->
        append(accNodeLog.getNodeLog(node))
        if (index != list.lastIndex) {
            appendLine()
        }
        accNodeLog.outBuilder.clear()
    }
}

/**转换成矩形坐标*/
fun List<AccessibilityNodeInfoCompat>.toRect(): List<Rect> {
    val result = mutableListOf<Rect>()
    forEach {
        result.add(Rect(it.bounds()))
    }
    return result
}

fun AccessibilityNodeInfoCompat.childList(): List<AccessibilityNodeInfoCompat> {
    val result = mutableListOf<AccessibilityNodeInfoCompat>()
    for (i in 0 until childCount) {
        getChild(i)?.let {
            result.add(it)
        }
    }
    return result
}

fun String.toPointF(width: Int = _screenWidth, height: Int = _screenHeight): PointF {
    val p = PointF()
    var x = 0f
    var y = 0f

    split(",").apply {
        x = getOrNull(0).getFloatNum() ?: x
        y = getOrNull(1).getFloatNum() ?: y
    }

    p.x = x.toPointF(width)
    p.y = y.toPointF(height)

    return p
}

fun Float.toPointF(ref: Int): Float {
    return if (this <= 1f) {
        ref * this
    } else {
        this * dp
    }
}