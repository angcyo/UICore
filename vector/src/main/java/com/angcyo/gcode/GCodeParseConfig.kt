package com.angcyo.gcode

/**
 * GCode解析配置数据类
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/05/13
 */
data class GCodeParseConfig(
    val text: String, //需要解析的GCode文本
    val mmRatio: Float, //G21 毫米指令对应的缩放比例
    val inRatio: Float, //G20 英寸指令对应的缩放比例
)
