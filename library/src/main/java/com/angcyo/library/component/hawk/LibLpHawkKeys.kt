package com.angcyo.library.component.hawk

import androidx.annotation.Keep
import com.angcyo.library.annotation.MM

/**
 * LaserPacker相关配置参数
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2022/12/31
 * Copyright (c) 2020 angcyo. All rights reserved.
 */

@Keep
object LibLpHawkKeys {

    //---

    /**为滑台重复图片间距。单位mm,保留一位小数。*/
    @MM
    var lastSlipSpace: Float by HawkPropertyValue<Any, Float>(10.8f)

    /**
     * 支持的固件范围
     * resValue "string", "lp_support_firmware", '"650~699 6500~6599 700~799 7000~7999"'
     * */
    var lpSupportFirmware: String? by HawkPropertyValue<Any, String?>(null)

    /**是否激活圆弧输出矢量数据
     * [com.angcyo.vector.VectorWriteHandler._valueChangedType]*/
    var enableVectorArc: Boolean by HawkPropertyValue<Any, Boolean>(false)

    /**是否激活压缩GCode输出*/
    var enableGCodeShrink: Boolean by HawkPropertyValue<Any, Boolean>(true)

    /**是否激活GCode最后的G0输出*/
    var enableGCodeEndG0: Boolean by HawkPropertyValue<Any, Boolean>(false)

    //---

    /**L1的物理尺寸配置*/
    @MM
    var l1Width: Int? by HawkPropertyValue<Any, Int?>(null)//100
    var l1Height: Int? by HawkPropertyValue<Any, Int?>(null)//100

    /**L2的物理尺寸配置*/
    @MM
    var l2Width: Int? by HawkPropertyValue<Any, Int?>(null)//100
    var l2Height: Int? by HawkPropertyValue<Any, Int?>(null)//100

    /**L3的物理尺寸配置*/
    @MM
    var l3Width: Int? by HawkPropertyValue<Any, Int?>(null)//115
    var l3Height: Int? by HawkPropertyValue<Any, Int?>(null)//115

    /**L4的物理尺寸配置*/
    @MM
    var l4Width: Int? by HawkPropertyValue<Any, Int?>(null)//160
    var l4Height: Int? by HawkPropertyValue<Any, Int?>(null)//160

    /**C1的物理尺寸配置*/
    @MM
    var c1Width: Int? by HawkPropertyValue<Any, Int?>(null)//400
    var c1Height: Int? by HawkPropertyValue<Any, Int?>(null)//420

    /**C1加长版的尺寸*/
    var c1LHeight: Int? by HawkPropertyValue<Any, Int?>(null)//800

    //---

    /**滚动轴(z轴)模式下, 最大的y值
     * 2米, mm单位, Z轴最大的Y坐标
     * 2m = 2000mm*/
    @MM
    var zMaxY: Int? by HawkPropertyValue<Any, Int?>(null)//2_00_0

    /**旋转轴(r轴)模式下, 最大的y值
     * 200 * 3.14 [Math.PI]*/
    @MM
    var rMaxY: Int? by HawkPropertyValue<Any, Int?>(null)//628

    /**滑台模式(s轴)*/
    @MM
    var sMaxY: Int? by HawkPropertyValue<Any, Int?>(null)//300

    /**C1移动平台/小车模式 (car模式)下, 最大的y值
     * 2米*/
    @MM
    var carMaxY: Int? by HawkPropertyValue<Any, Int?>(null)//2_00_0

    //

    /**是否激活wifi传输*/
    var enableWifiConfig: Boolean by HawkPropertyValue<Any, Boolean>(false)

    /**服务地址: ip:port*/
    var wifiAddress: String? by HawkPropertyValue<Any, String?>(null)

    /**wifi发送缓存大小*/
    var wifiBufferSize: Int by HawkPropertyValue<Any, Int>(4096)

    /**wifi发送延迟*/
    var wifiSendDelay: Int by HawkPropertyValue<Any, Int>(0)

    /**是否激活多选时元素边框的绘制*/
    var enableRenderElementBounds: Boolean by HawkPropertyValue<Any, Boolean>(true)
}