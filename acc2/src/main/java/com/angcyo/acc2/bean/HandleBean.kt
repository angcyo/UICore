package com.angcyo.acc2.bean

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/01/30
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
data class HandleBean(
    /**不管执行有没有成功, 都返回[false]
     * 优先处理此属性*/
    var ignore: Boolean = false,

    /**不管执行是否成功, 都跳过之后的[HandleBean]处理*/
    var jump: Boolean = false,

    /**当执行成功后, 跳过之后的[HandleBean]处理*/
    var jumpOnSuccess: Boolean = false,
)
