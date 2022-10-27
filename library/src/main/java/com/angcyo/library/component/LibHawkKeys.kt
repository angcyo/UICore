package com.angcyo.library.component

import androidx.annotation.Keep
import com.angcyo.library.annotation.MM

/**
 * 内部库中的一些持久化数据
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/10/18
 */

@Keep
object LibHawkKeys {

    /**[com.angcyo.component.luban.DslLuban]
     * 压缩时, 最小的压缩像素大小 [kb]
     * */
    var minKeepSize: Int by HawkPropertyValue<Any, Int>(400)

    /**为滑台重复图片间距。单位mm,保留一位小数。*/
    @MM
    var lastSlipSpace: Float by HawkPropertyValue<Any, Float>(10.8f)

}