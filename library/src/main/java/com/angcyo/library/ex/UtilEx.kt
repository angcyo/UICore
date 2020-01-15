package com.angcyo.library.ex

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/01/15
 */

fun <T> ArrayList<T>.append(element: T, maxSize: Int = 10) {
    if (size >= maxSize) {
        removeAt(0)
    }
    add(element)
}

fun <T> MutableList<T>.append(element: T, maxSize: Int = 10) {
    if (size >= maxSize) {
        removeAt(0)
    }
    add(element)
}