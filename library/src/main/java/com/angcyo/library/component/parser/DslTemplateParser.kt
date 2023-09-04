package com.angcyo.library.component.parser

import com.angcyo.library.annotation.CallPoint
import com.angcyo.library.annotation.DSL

/**
 * 模板解析器,
 * 将字符放在方括号中，即可原样返回而不被格式化替换 (例如， [MM])。
 *
 * YYYY-MM-DD HH:mm:ss
 *
 * https://dayjs.fenxianglu.cn/category/display.html#%E6%A0%BC%E5%BC%8F%E5%8C%96
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2023/09/02
 */
open class DslTemplateParser {

    /**返回需要替换的模板字符串*/
    var replaceTemplateAction: (template: String) -> String = { template -> template }

    /**返回需要替换的变量字符串*/
    var replaceVariableAction: (variable: String) -> String? = { variable ->
        null
        //{ variable -> "${variableStart}$variable${variableEnd}" }
    }

    /**是否是相同的模板字符*/
    var isSameTemplateCharAction: (before: Char?, after: Char) -> Boolean = { before, after ->
        if (before == null) {
            true
        } else {
            before == after
        }
    }

    /**特殊模板列表, 关键字列表
     * ```
     * weekday    //星期几 从0开始, 国际化
     * dayOfYear  //1年中的第几天 从1开始
     * weekOfYear //1年中的第几周 从1开始
     * ```
     * */
    var templateList = mutableListOf<String>()

    /**返回值存放*/
    protected val parseResult = StringBuilder()

    /**当前解析到char索引*/
    protected var index = 0

    /**变量开始的字符标识*/
    protected var variableStart = "{{"
    protected var variableEnd = "}}"

    /**开始解析字符串[text]
     * [YYYYescape] YYYY-MM-DDTHH:mm:ssZ[Z]
     * */
    @CallPoint
    fun parse(text: String): String {
        while (index < text.length) {
            val startIndex = index
            val variable = findVariable(text, text[index])
            if (variable != null) {
                val variableStr = replaceVariableAction(variable)
                if (variableStr == null) {
                    //没有处理变量, 则忽略变量的处理, 进行下一步模板的处理
                    index = startIndex
                } else {
                    parseResult.append(variableStr)
                }
            }
            nextTemplate(text)?.let {
                parseResult.append(replaceTemplateAction(it))
            }
            index++
        }
        return parseResult.toString()
    }

    /**下一段需要解析或者需要原样输出的模板*/
    protected fun nextTemplate(text: String): String? {
        val oldIndex = index
        val template = StringBuilder()
        var lastChar: Char? = null
        while (index < text.length) {
            val char = text[index]

            val find = findTemplate(text, char)
            if (find != null) {
                //找到了模板
                if (template.isNotEmpty()) {
                    parseResult.append(replaceTemplateAction(template.toString()))
                }
                return find
            }

            if (isSameTemplateChar(lastChar, char).not()) {
                //不相同的模板字符, 结束
                index = maxOf(oldIndex, index - 1)
                return template.toString()
            }

            if (char == '[') {
                //开始解析
                val endIndex = getNextCharIndex(text, ']')
                if (endIndex != -1) {
                    //[[xxxMM] 结构
                    parseResult.append(text.substring(index + 1, endIndex))
                    index = endIndex
                    return null
                } else {
                    //没有找到结束字符],则当做普通字符串处理
                    template.append(char)
                }
            } else {
                template.append(char)
            }

            lastChar = char
            index++
        }
        return template.toString()
    }

    /**查找变量表达式*/
    protected fun findVariable(text: String, char: Char): String? {
        if (variableStart.startsWith(char)) {
            val oldIndex = index
            val startIndex = getNextStringIndex(text, variableStart)
            if (startIndex != -1) {
                index = startIndex + variableStart.length
                val endIndex = getNextStringIndex(text, variableEnd)
                if (endIndex != -1) {
                    index = endIndex + variableEnd.length
                    return text.substring(startIndex + variableStart.length, endIndex)
                }
            }
            index = oldIndex
        }
        return null
    }

    /**查找当前字符是否有匹配的模板字符串*/
    private fun findTemplate(text: String, char: Char): String? {
        for (template in templateList) {
            if (template.startsWith(char)) {
                val endIndex = index + template.length
                if (endIndex > text.length) {
                    break
                }
                val target = text.substring(index, endIndex)
                if (target == template) {
                    index = endIndex - 1
                    return template
                }
            }
        }
        return null
    }

    private fun isSameTemplateChar(before: Char?, after: Char): Boolean {
        return isSameTemplateCharAction(before, after)
    }

    /**获取下一个指定字符的索引*/
    private fun getNextCharIndex(text: String, char: Char): Int {
        var resultIndex = -1
        for (i in index until text.length) {
            if (text[i] == char) {
                resultIndex = i
                break
            }
        }
        return resultIndex
    }

    private fun getNextStringIndex(text: String, str: String): Int {
        var resultIndex = -1
        for (i in index until text.length) {
            val endIndex = i + str.length
            if (endIndex > text.length) {
                break
            }
            if (text.substring(i, endIndex) == str) {
                resultIndex = i
                break
            }
        }
        return resultIndex
    }
}

/**解析模板*/
@DSL
fun String.parseTemplate(replaceTemplateAction: (template: String) -> String = { template -> template }): String {
    return DslTemplateParser().apply {
        this.replaceTemplateAction = replaceTemplateAction
    }.parse(this)
}