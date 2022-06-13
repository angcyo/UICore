package com.angcyo.library.annotation

/**
 * 标注功能正在实现中...
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2022/06/13
 * Copyright (c) 2020 angcyo. All rights reserved.
 */

@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.FIELD,
    AnnotationTarget.CLASS,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.TYPE,
)
@Retention(AnnotationRetention.SOURCE)
annotation class Implementation
