package com.angcyo.acc2.bean

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/01/30
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
data class HandleBean(

    /**
     * 满足其中一个条件,[HandleBean]才会被执行*/
    var conditionList: List<ConditionBean>? = null,

    /**重新选择新的元素, 否则直接使用[com.angcyo.acc2.bean.CheckBean.event]获取到的元素*/
    var findList: List<FindBean>? = null,

    /**过滤目标元素, 只处理之后的元素*/
    var filter: FilterBean? = null,

    /**不管执行有没有成功, 都返回[false]
     * 优先处理此属性*/
    var ignore: Boolean = false,

    /**不管执行是否成功, 都跳过之后的[HandleBean]处理*/
    var jump: Boolean = false,

    /**当执行成功后, 跳过之后的[HandleBean]处理*/
    var jumpOnSuccess: Boolean = false,

    /**
     * 当有元素选择时需要执行的具体操作
     * [com.angcyo.acc2.action.Action]*/
    var actionList: List<String>? = null,

    /**
     * 当没有元素选择时需要执行的具体操作, 如果未指定则继续使用[actionList]
     * [com.angcyo.acc2.action.Action]*/
    var noActionList: List<String>? = null,

    /**[actionList] [noActionList] 执行失败时, 需要执行的动作. 不受[ignore]影响*/
    var failActionList: List<String>? = null,

    /**[actionList] [noActionList] 执行成功时, 需要执行的动作. 不受[ignore]影响*/
    var successActionList: List<String>? = null,
)
