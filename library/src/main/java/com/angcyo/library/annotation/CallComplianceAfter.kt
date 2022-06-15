package com.angcyo.library.annotation

/**
 * 标识方法需要合规后调用
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/06/15
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class CallComplianceAfter