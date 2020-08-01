package com.angcyo.core.component.accessibility.action

/**
 * [Action]执行异常
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/07/01
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
open class ActionException(msg: String) : Exception(msg)

open class ActionInterruptedException(msg: String) : ActionException(msg)