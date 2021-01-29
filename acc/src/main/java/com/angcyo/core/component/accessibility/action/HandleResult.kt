package com.angcyo.core.component.accessibility.action

/**
 * [com.angcyo.core.component.accessibility.action.AutoParseAction.handleAction]
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/08/01
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
data class HandleResult(
    //当前handle的结果
    var result: Boolean = false,
    //是否跳过之后的handle
    var jumpNextHandle: Boolean = false,
    //是否需要直接执行finish当前的action
    var finish: Boolean = false
)