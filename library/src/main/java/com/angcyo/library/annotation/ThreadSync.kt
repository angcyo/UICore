package com.angcyo.library.annotation

/**
 * 标识当前的方法需要在线程内同步执行
 *
 * [com.angcyo.library.ex.syncSingle]
 *
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2023/07/27
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class ThreadSync
