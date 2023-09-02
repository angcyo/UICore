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

    /**是否是相同的模板字符*/
    var isSameTemplateCharAction: (before: Char?, after: Char) -> Boolean = { before, after ->
        if (before == null) {
            true
        } else {
            before == after
        }
    }

    /**返回值存放*/
    protected val parseResult = StringBuilder()

    /**当前解析到char索引*/
    protected var index = 0

    /**开始解析字符串[text]
     * [YYYYescape] YYYY-MM-DDTHH:mm:ssZ[Z]
     * */
    @CallPoint
    fun parse(text: String): String {
        while (index < text.length) {
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

            if (isSameTemplateCharAction(lastChar, char).not()) {
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
}

/**解析模板*/
@DSL
fun String.parseTemplate(replaceTemplateAction: (template: String) -> String = { template -> template }): String {
    return DslTemplateParser().apply {
        this.replaceTemplateAction = replaceTemplateAction
    }.parse(this)
}