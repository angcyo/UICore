package com.angcyo.acc2.parse

import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.angcyo.acc2.action.Action
import com.angcyo.acc2.core.AccNodeLog
import com.angcyo.library.ex.have
import com.angcyo.library.ex.patternList

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
fun String.arg(key: String, delimiters: Char = ':') = Action.patternArg(this, key, delimiters)

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
fun String?.havePartition(text: CharSequence = "~") = have(text)

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

/**转成日志*/
fun List<AccessibilityNodeInfoCompat>.toLog(header: String?) = buildString {
    if (header != null) {
        appendLine(header)
    }
    val accNodeLog = AccNodeLog()
    accNodeLog.logNodeChild = false
    this@toLog.forEach {
        appendLine(accNodeLog.getNodeLog(it.unwrap()))
    }
}