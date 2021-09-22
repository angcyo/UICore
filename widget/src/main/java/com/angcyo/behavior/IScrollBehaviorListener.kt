package com.angcyo.behavior

/**
 * [BaseScrollBehavior]滚动监听
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/01/01
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */

interface IScrollBehaviorListener {

    /**[com.angcyo.behavior.BaseScrollBehavior.scrollTo]*/
    fun onBehaviorScrollTo(scrollBehavior: BaseScrollBehavior<*>, x: Int, y: Int, scrollType: Int) {
        //default
    }

    /**
     * [scrollType]滚动类型,
     * [endType]滚动结束类型, 是内嵌滚动结束, 还是touch结束. 数值和[scrollType]一致*/
    fun onBehaviorScrollStop(scrollBehavior: BaseScrollBehavior<*>, scrollType: Int, endType: Int) {
        //default
    }
}