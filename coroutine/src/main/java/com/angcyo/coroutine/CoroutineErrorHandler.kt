package com.angcyo.coroutine

import com.angcyo.library.L
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

/**
 * 协程异常处理
 *
 * [协程异常处理](https://www.kotlincn.net/docs/reference/coroutines/exception-handling.html)
 *
 * 在协程内发生的异常, 如果被协程捕捉到, 只能通过[CoroutineExceptionHandler]消费异常.
 *
 * try只能捕捉,不能消费
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/12/25
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
open class CoroutineErrorHandler(
    val action: (exception: Throwable) -> Unit = {
        L.e("协程内发生异常->\n${it.stackTraceToString()}")
        it.printStackTrace()
    }
) : AbstractCoroutineContextElement(CoroutineExceptionHandler), CoroutineExceptionHandler {
    override fun handleException(context: CoroutineContext, exception: Throwable) {
        action(exception)
    }
}