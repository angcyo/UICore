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

    /**是否直接通过匹配*/
    var pass: Boolean = false,

    /**直接跳过后续的[ChildBean]匹配, 之前匹配的结果直接返回.
     * 本次匹配还是会正常进行*/
    var jump: Boolean = false,

    /**如果匹配通过了, 则正常流程继续匹配,
     * 如果匹配失败了, 则忽略这个匹配项, 这个匹配项当做不存在, 继续剩下的匹配*/
    var ignore: Boolean = false,
)
