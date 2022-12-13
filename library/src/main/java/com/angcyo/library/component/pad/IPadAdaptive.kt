package com.angcyo.library.component.pad

import com.angcyo.library.app

/**
 * 平板适配
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/12/13
 */
interface IPadAdaptive {

    /**是否要适配平板*/
    fun enablePadAdaptive() = false

    /**是否处于平板模式*/
    fun isPadMode() = Pad.isPad
}

/**pad模式*/
fun isInPadMode() = isEnablePadAdaptive() && isPadMode()

/**[com.angcyo.library.component.pad.IPadAdaptive.enablePadAdaptive]*/
fun isEnablePadAdaptive(): Boolean {
    val app = app()
    return if (app is IPadAdaptive) app.enablePadAdaptive() else false
}

/**[com.angcyo.library.component.pad.IPadAdaptive.isPadMode]*/
fun isPadMode(): Boolean {
    val app = app()
    return if (app is IPadAdaptive) app.isPadMode() else false
}