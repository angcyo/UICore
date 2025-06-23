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

    /**智能推荐的最小速率*/
    var minimumSmartAssistantVelocity: Float by HawkPropertyValue<Any, Float>(100f)

    /**是否激活Path数据的弧度采样方式.
     * 等距采样: 每隔相同的点采样一次, 并计算
     * 弧度采样: 每隔相同的点采样一次, 但是相同的弧度才计算一次. 弧度采样模式下不会输出G2/G3数据
     * [LibHawkKeys.pathSampleStepRadians]
     * [LibHawkKeys.pathTolerance]
     * */
    var enableVectorRadiansSample: Boolean by HawkPropertyValue<Any, Boolean>(true)

    //---

    /**L1的物理尺寸配置*/
    @MM
    var l1Width: Float? by HawkPropertyValue<Any, Float?>(null)//100
    var l1Height: Float? by HawkPropertyValue<Any, Float?>(null)//100

    /**L2的物理尺寸配置*/
    @MM
    var l2Width: Float? by HawkPropertyValue<Any, Float?>(null)//100
    var l2Height: Float? by HawkPropertyValue<Any, Float?>(null)//100

    /**L3的物理尺寸配置*/
    @MM
    var l3Width: Float? by HawkPropertyValue<Any, Float?>(null)//115
    var l3Height: Float? by HawkPropertyValue<Any, Float?>(null)//115

    /**L4的物理尺寸配置*/
    @MM
    var l4Width: Float? by HawkPropertyValue<Any, Float?>(null)//160
    var l4Height: Float? by HawkPropertyValue<Any, Float?>(null)//160

    /**C1的物理尺寸配置*/
    @MM
    var c1Width: Float? by HawkPropertyValue<Any, Float?>(null)//400
    var c1Height: Float? by HawkPropertyValue<Any, Float?>(null)//420

    /**C1加长版的尺寸*/
    var c1LHeight: Float? by HawkPropertyValue<Any, Float?>(null)//800

    //

    /**是否激活强制wifi传输, 调试界面*/
    var enableWifiConfig: Boolean by HawkPropertyValue<Any, Boolean>(false)

    /**强制使用wifi连接的 服务地址: ip:port*/
    var wifiAddress: String? by HawkPropertyValue<Any, String?>(null)

    /**wifi发送缓存大小*/
    var wifiBufferSize: Int by HawkPropertyValue<Any, Int>(4096)

    /**wifi发送延迟
     * [com.angcyo.http.tcp.Tcp.sendDelay]*/
    var wifiSendDelay: Long by HawkPropertyValue<Any, Long>(0)

    /**[com.angcyo.http.tcp.Tcp.sendDelayByteCount]*/
    var wifiSendDelayByteCount: Long by HawkPropertyValue<Any, Long>(30 * 1024)

    /**是否激活多选时元素边框的绘制*/
    var enableRenderElementBounds: Boolean by HawkPropertyValue<Any, Boolean>(true)

    /**是否优先使用ip连接设备*/
    var enableUseIpConnect: Boolean by HawkPropertyValue<Any, Boolean>(true)
}