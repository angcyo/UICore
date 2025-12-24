package com.angcyo.library.annotation

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2025-03-12
 */
@MustBeDocumented
@Retention(AnnotationRetention.SOURCE)
@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.FIELD,
    AnnotationTarget.LOCAL_VARIABLE,
    AnnotationTarget.VALUE_PARAMETER,
    AnnotationTarget.EXPRESSION,
)
annotation class Sign(val des: String = "特性加入的描述" /*简单的描述*/)
