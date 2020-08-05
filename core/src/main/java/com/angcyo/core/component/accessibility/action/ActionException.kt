package com.angcyo.core.component.accessibility.action

/**
 * [Action]执行异常
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/07/01
 * Copyright (c) 2020 angcyo. All rights reserved.
 */

//异常
open class ActionException(msg: String?) : Exception(msg)

//中断异常
open class ActionInterruptedException(msg: String?) : ActionException(msg)

//中断后, 需要继续
open class ActionInterruptedNextException(msg: String?) : ActionInterruptedException(msg)