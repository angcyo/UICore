package com.angcyo.acc2.bean

import com.angcyo.acc2.parse.AccParse
import com.angcyo.acc2.parse.getIntList
import com.angcyo.library.ex.size

/**
 * [ActionBean]循环控制器, 只有循环控制失败后, 才会执行原有的逻辑.
 * 否则无限重复执行当前的[ActionBean].
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/04/12
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
data class LoopBean(

    /**是否要验证数据结构, 结构无效跳过Loop*/
    var valid: Boolean = false,

    /**如果为true, 那么只有在[ActionBean]执行成功时, 才会进入循环解析.
     * 否则任意情况下都会进入循环解析*/
    var check: Boolean = true,

    /**循环多少次之后, 强制正常退出循环. 支持文本变量
     * "3":循环3次
     * "3~10":循环[3~10]次
     * */
    var success: String? = null,

    /**循环多少次之后, 强制异常退出循环. 支持文本变量*/
    var error: String? = null,

    //----------------------------------------------------------------

    /**Loop操作时, 需要进行的处理.*/
    var handle: List<HandleBean>? = null,

    /**Loop退出时, 需要进行的处理.
     * 默认是next()处理*/
    var exit: List<HandleBean>? = null,
)

/**数据结构是否有效, 无效的数据结构, 跳过执行*/
fun LoopBean.isLoopValid(accParse: AccParse): Boolean {

    if (success != null) {
        val count = accParse.textParse.parse(success).firstOrNull()
        val numList = count.getIntList()
        if (numList.isEmpty() || (numList.size() == 1 && numList.firstOrNull() ?: 0 < 0)) {
            //小于0的数, 则无效
            return false
        }
    }

    if (error != null) {
        val count = accParse.textParse.parse(error).firstOrNull()
        val numList = count.getIntList()
        if (numList.isEmpty() || (numList.size() == 1 && numList.firstOrNull() ?: 0 < 0)) {
            //小于0的数, 则无效
            return false
        }
    }

    return true
}