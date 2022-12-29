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

    /**当颜色的透明值小于此值时, 视为透明色*/
    var alphaThreshold: Int by HawkPropertyValue<Any, Int>(8)

    /**灰度阈值, 大于这个值视为白色*/
    var grayThreshold: Int by HawkPropertyValue<Any, Int>(128)

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

    /**
     * 哪些固件范围的中心点在物理中心
     * resValue "string", "lp_device_origin_center", '"250~252 270~270 300~313 350~357 370~372 5500~5507 5510~5512"'
     * */
    var lpDeviceOriginCenter: String? by HawkPropertyValue<Any, String?>(null)

    /**允许最大分配的图片大小,
     * 10mb 10 * 1024 * 1024 => 10,485,760
     * 64,000,000 61mb
     *
     * pixel 6 max:174 584 760 bytes
     * */
    var maxBitmapCanvasSize: Long by HawkPropertyValue<Any, Long>(60 * 1024 * 1024)

    /**是否激活圆弧输出矢量数据
     * [com.angcyo.vector.VectorWriteHandler._valueChangedType]*/
    var enableVectorArc: Boolean by HawkPropertyValue<Any, Boolean>(false)

    /**[com.angcyo.canvas.CanvasDelegate]允许添加的最大渲染元素数据*/
    var canvasRenderMaxCount: Int by HawkPropertyValue<Any, Int>(30)
}