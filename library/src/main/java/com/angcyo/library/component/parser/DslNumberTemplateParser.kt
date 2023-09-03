package com.angcyo.library.component.parser

import com.angcyo.library.annotation.CallPoint
import com.angcyo.library.annotation.DSL
import kotlin.math.max

/**
 * 数字模板解析器
 *
 * `#` 代表数字占位符
 * `,` 代表分隔符
 * `.` 代表小数点
 * `0` 代表补位
 * 其他表示原样输出
 *
 * ```
 * #G#,###,000.##
 * ```
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2023/09/03
 */
class DslNumberTemplateParser {

    /**小数点分割符*/
    var decimalPointChar = '.'

    /**分隔符集合*/
    val splitCharList = mutableListOf(',')

    /**补齐字符集合*/
    val padCharList = mutableListOf('0')

    /**占位字符集合*/
    val placeholderCharList = mutableListOf('#')

    /**
     * [number] 需要格式化的数字
     * [template] 模板
     * */
    @CallPoint
    fun parse(number: String, template: String): String {
        val numberList = number.split(decimalPointChar)
        val intPart = numberList.getOrNull(0)?.reversed() //倒序
        val decimalPart = numberList.getOrNull(1)

        val templateList = template.split(decimalPointChar)
        val intTemplate = templateList.getOrNull(0)?.reversed() //倒序
        val decimalTemplate = templateList.getOrNull(1)

        //整数部分结果
        val intResult = parseTemplate(intPart, intTemplate).reversed()
        //小数部分结果
        val decimalResult = parseTemplate(decimalPart, decimalTemplate)

        return if (decimalResult.isEmpty() || decimalPart == null) {
            intResult
        } else {
            "$intResult$decimalPointChar$decimalResult"
        }
    }

    private fun parseTemplate(text: String?, template: String?): String {
        val result = StringBuilder()
        var templateIndex = 0
        for (i in 0 until max(text?.length ?: 0, template?.length ?: 0)) {
            val intChar = text?.getOrNull(i)
            var templateChar = template?.getOrNull(templateIndex)
            if (intChar == null && templateChar == null) {
                break
            }
            if (intChar == null) {
                //数字不够, 补齐
                while (padCharList.contains(templateChar)) {
                    //是补齐符
                    result.append(templateChar)
                    templateIndex++
                    templateChar = template?.getOrNull(templateIndex)

                    if (splitCharList.contains(templateChar)) {
                        //是分隔符
                        result.append(templateChar)
                        while (splitCharList.contains(templateChar)) {
                            templateIndex++
                            templateChar = template?.getOrNull(templateIndex)
                        }
                    }
                }
                break
            }
            if (templateChar == null) {
                //模板不够, 原样输出
                result.append(intChar)
                continue
            }

            if (splitCharList.contains(templateChar)) {
                //是分隔符
                result.append(templateChar)
                while (splitCharList.contains(templateChar)) {
                    templateIndex++
                    templateChar = template?.getOrNull(templateIndex)
                }
            }

            if (placeholderCharList.contains(templateChar) || padCharList.contains(templateChar)) {
                result.append(intChar)
            } else {
                result.append(templateChar)
            }

            templateIndex++
        }

        return result.toString()
    }

}

/**解析数字模板*/
@DSL
fun String.parseNumberTemplate(number: String): String {
    return DslNumberTemplateParser().parse(number, this)
}