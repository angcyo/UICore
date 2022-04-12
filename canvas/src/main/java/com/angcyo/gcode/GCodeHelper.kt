package com.angcyo.gcode

import android.content.Context
import android.util.DisplayMetrics
import android.util.TypedValue
import androidx.annotation.WorkerThread

/**
 *
 * https://reprap.org/wiki/G-code/zh_cn
 * https://ncviewer.com/
 *
 * @author <a href="mailto:angcyo@126.com">angcyo</a>
 * @since 2022/04/12
 */
object GCodeHelper {

    fun parseGCode(context: Context, text: String): GCodeDrawable {
        //1毫米等于多少像素
        val dm: DisplayMetrics = context.resources.displayMetrics
        val mmPixel = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, 1f, dm) //21.176456

        //1英寸等于多少像素, 1英寸=2.54厘米=25.4毫米
        val inPixel = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_IN, 1f, dm) //537.882
        return parseGCode(text, mmPixel, inPixel)
    }

    /**
     * [mmRatio] 毫米单位时, 需要放大的比例
     * [inRatio] 英寸单位时, 需要放大的比例
     * */
    @WorkerThread
    fun parseGCode(text: String, mmRatio: Float, inRatio: Float): GCodeDrawable {
        val gCodeLineDataList = mutableListOf<GCodeLineData>()
        var currentRatio = mmRatio // 默认使用毫米单位
        text.lines().forEach { line ->
            val gCodeLineData = _parseGCodeLine(line, mmRatio, inRatio, currentRatio)
            gCodeLineDataList.add(gCodeLineData)

            //ratio
            gCodeLineData.list.findLast {
                if (it.cmd.isGCodeMoveDirective()) {
                    currentRatio = it.ratio
                    true
                } else {
                    false
                }
            }
        }
        return GCodeDrawable(gCodeLineDataList)
    }

    fun _parseGCodeLine(
        line: String,
        mmRatio: Float,
        inRatio: Float,
        currentRatio: Float //当前使用的比例
    ): GCodeLineData {
        val cmdList = mutableListOf<GCodeCmd>()

        var ratio = currentRatio

        //注释
        var comment: String? = null

        var cmdString: String = line
        val commentIndex = line.indexOf(";")
        if (commentIndex == -1) {
            //无注释
        } else {
            //有注释
            cmdString = line.substring(0, commentIndex)
            comment = line.substring(commentIndex + 1, line.length)
        }

        val cmdStringList = cmdString.split(' ')
        val lineFirstCmd: String = cmdStringList.firstOrNull() ?: ""
        cmdStringList.forEach { cmd ->
            if (cmd.isNotEmpty()) {
                val cmdBuilder = StringBuilder()
                val numberBuilder = StringBuilder()
                var isCmdChar = true

                cmd.forEach { char ->
                    val charInt = char.code
                    if (isCmdChar) {
                        if (charInt in 65..90) {
                            //大写字母A~Z
                            cmdBuilder.append(char)
                        } else {
                            isCmdChar = false
                        }
                    }
                    if (!isCmdChar) {
                        numberBuilder.append(char)
                    }
                }

                val cmdStr = cmdBuilder.toString()
                //单位切换
                if (cmdStr == "G20") {
                    ratio = inRatio
                } else if (cmdStr == "G21") {
                    ratio = mmRatio
                }

                //转成数字后, 字符前面的0会丢失
                val number = numberBuilder.toString().toFloatOrNull() ?: 0f
                val gCodeCmd = if (lineFirstCmd.isGCodeMoveDirective()) {
                    GCodeCmd(cmdStr, number, ratio, ratio * number)
                } else {
                    GCodeCmd(cmdStr, number, ratio)
                }
                cmdList.add(gCodeCmd)
            }
        }
        return GCodeLineData(cmdList, comment)
    }
}