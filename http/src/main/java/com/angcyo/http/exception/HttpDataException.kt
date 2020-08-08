package com.angcyo.http.exception

/**
 * Http 返回的 json 数据, code非200..299, 业务异常代码.
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/12/31
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
open class HttpDataException(msg: String, val code: Int = -1) : RuntimeException(msg)