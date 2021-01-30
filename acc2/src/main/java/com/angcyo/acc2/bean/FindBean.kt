package com.angcyo.acc2.bean

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/01/30
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
data class FindBean(

    /**在那个[AccessibilityWindowInfo]中获取节点
     * 不指定, 则根据包名默认处理*/
    var window: WindowBean? = null,

    /**通过文本获取节点,
     * 支持变量从[TaskBean]中获取值
     * 支持正则
     * */
    var textList: List<String>? = null
)
