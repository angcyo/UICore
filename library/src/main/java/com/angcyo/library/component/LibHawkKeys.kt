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

    /**日志单文件最大数据量的大小
     * 允许写入单个文件的最大大小10mb, 之后会重写*/
    var logFileMaxSize: Long by HawkPropertyValue<Any, Long>(2 * 1024 * 1024)

    /**
     * 支持的固件范围
     * resValue "string", "lp_support_firmware", '"650~699 6500~6599 700~799 7000~7999"'
     * */
    var lpSupportFirmware: String? by HawkPropertyValue<Any, String?>(null)

    /**允许最大分配的图片大小,
     * 10mb 10 * 1024 * 1024 => 10,485,760
     * 64,000,000 61mb
     * */
    var maxBitmapCanvasSize: Long by HawkPropertyValue<Any, Long>(10 * 1024 * 1024)

}