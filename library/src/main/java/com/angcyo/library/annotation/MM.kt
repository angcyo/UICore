package com.angcyo.library.annotation

/**
 * 毫米单位的值
 * [com.angcyo.library.unit.MmValueUnit]
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/09/20
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
annotation class MM
