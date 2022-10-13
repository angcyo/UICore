package com.angcyo.library.annotation

/**
 * 标识
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022-10-13
 */

@MustBeDocumented
@Retention(AnnotationRetention.SOURCE)
@Target(
    AnnotationTarget.PROPERTY,
    AnnotationTarget.FIELD,
    AnnotationTarget.LOCAL_VARIABLE,
    AnnotationTarget.VALUE_PARAMETER
)
annotation class Flag(val des: String = "" /*简单的描述*/)
