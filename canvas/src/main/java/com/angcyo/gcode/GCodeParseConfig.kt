package com.angcyo.gcode

import android.graphics.Paint

/**
 * GCode解析配置数据类
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/05/13
 */
data class GCodeParseConfig(
    val text: String, //需要解析的GCode文本
    val mmRatio: Float, //G21 毫米指令对应的缩放比例
    val inRatio: Float, //G20 英寸指令对应的缩放比例
    val skipLast: Boolean, //跳过绘制M5指令后的最后一条G1 X0 Y0指令
    val paint: Paint //绘制时用到的画笔
)
