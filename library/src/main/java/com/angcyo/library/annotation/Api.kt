package com.angcyo.library.annotation

/**
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2025/02/21
 */
@MustBeDocumented
@Retention(AnnotationRetention.SOURCE)
@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.FIELD,
    AnnotationTarget.LOCAL_VARIABLE,
    AnnotationTarget.VALUE_PARAMETER
)
annotation class Api
