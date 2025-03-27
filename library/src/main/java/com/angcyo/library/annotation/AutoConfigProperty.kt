package com.angcyo.library.annotation

/**
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @date 2025/03/27
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
annotation class AutoConfigProperty(val des: String = "描述当前属性为自动配置属性")