package com.angcyo.core.component.accessibility.parse

import com.angcyo.core.component.accessibility.back
import com.angcyo.core.component.accessibility.rootNodeInfo
import com.angcyo.core.component.accessibility.text

/**
 * 参数严格的约束
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/07/07
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
data class ConstraintBean(

    /**约束的文本, 这个文本可以是对应的id, 或者node上的文本内容
     * 文本需要全部命中.
     * 空字符会匹配[rootNodeInfo]
     * */
    var text: List<String>? = null,

    /**类名约束, 和[text]为一一对应的关系.为空, 表示不约束类名
     * 匹配规则时包含, 只要当前设置的cls包含视图中的cls就算命中.
     * 空字符会命中所有
     * */
    var cls: List<String>? = null,

    /**此约束需要执行的动作, 不指定坐标. 将随机产生. 小于1的数, 表示比例
     * [click] 触发当前节点的点击事件
     * [click2] 在当前节点区域双击
     * [longClick] 触发当前节点的长按事件
     * [touch:10,10] 在屏幕坐标x=10dp y=10dp的地方点击
     * [double:20,30] 在屏幕坐标x=20dp y=30dp的地方双击
     * [move:10,10-100,100] 从屏幕坐标x=10dp y=10dp的地方移动到100dp 100dp的地方
     * [fling:10,10-100,100]
     * [back] 执行返回操作
     * [getText] 获取文本内容
     * [setText] 设置文本内容
     * [random] 随机执行
     *
     * 空字符会进行随机操作.
     * null 默认是click操作
     * */
    var action: List<String>? = null,

    /**忽略此次[Action]操作的返回值, 不忽略的话, 如果action返回true, 则可能会执行[doActionFinish]*/
    var ignore: Boolean = false,

    /**
     * 坐标矩形约束. 格式10,10-100,100 小于1的数, 表示比例否则就是dp.
     * 空字符只要宽高大于0, 就命中
     * */
    var rect: List<String>? = null
) {
    companion object {
        const val ACTION_CLICK = "click"
        const val ACTION_CLICK2 = "click2"
        const val ACTION_LONG_CLICK = "longClick"
        const val ACTION_DOUBLE = "double"
        const val ACTION_TOUCH = "touch"
        const val ACTION_MOVE = "move"
        const val ACTION_FLING = "fling"
        const val ACTION_BACK = "back"
        const val ACTION_GET_TEXT = "getText"
        const val ACTION_SET_TEXT = "setText"
        const val ACTION_RANDOM = "random"
    }
}