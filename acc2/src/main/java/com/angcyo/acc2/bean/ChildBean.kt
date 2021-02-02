package com.angcyo.acc2.bean

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/02/02
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
data class ChildBean(

    /**[com.angcyo.acc2.bean.FindBean.textList]*/
    var text: String? = null,

    /**[com.angcyo.acc2.bean.FindBean.clsList]*/
    var cls: String? = null,

    /**[com.angcyo.acc2.bean.FindBean.idList]*/
    var id: String? = null,

    /**[com.angcyo.acc2.bean.FindBean.rectList]*/
    var rect: String? = null,

    /**[com.angcyo.acc2.bean.FindBean.stateList]*/
    var state: String? = null,

    /**
     * 继续约束节点满足指定child约束获取节点
     * */
    var childList: List<ChildBean>? = null,

    //------------------------------------------------------

    /**需要满足的过滤条件[com.angcyo.acc2.bean.FindBean.filter]*/
    var filter: FilterBean? = null,

    /**忽略当前child的匹配结果, 继续下一个child匹配*/
    var ignore: Boolean = false
)
