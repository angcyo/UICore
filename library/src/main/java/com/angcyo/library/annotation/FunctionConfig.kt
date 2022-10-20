package com.angcyo.library.annotation

/**
 * 功能开关配置, 描述当前属性是一个功能
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022-10-20
 */

@MustBeDocumented
@Retention(AnnotationRetention.SOURCE)
@Target(
    AnnotationTarget.PROPERTY,
    AnnotationTarget.FIELD,
)
annotation class FunctionConfig(val des: String = "" /*功能的描述*/)
