package com.angcyo.acc2.parse

import com.angcyo.acc2.action.Action
import com.angcyo.acc2.action.InputAction
import com.angcyo.acc2.bean.TextParamBean
import com.angcyo.acc2.bean.getTextList
import com.angcyo.acc2.control.AccControl
import com.angcyo.acc2.control.log
import com.angcyo.library.app
import com.angcyo.library.component.appBean
import com.angcyo.library.ex.*
import com.angcyo.library.utils.getLongNum

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/03/05
 * Copyright (c) 2020 angcyo. All rights reserved.
 */
class TextParse(val accParse: AccParse) : BaseParse() {

    val accControl: AccControl
        get() = accParse.accControl

    val findParse: FindParse
        get() = accParse.findParse

    /**支持默认值[defKey]*/
    fun parseOrDef(
        arg: String?,
        defKey: String? = Action.DEF,
        replace: Boolean = false,
        textParamBean: TextParamBean? = null
    ): List<String?> {
        if (arg.isNullOrEmpty() || defKey.isNullOrEmpty()) {
            return parse(arg, replace, textParamBean)
        } else {
            parseTextKeyAndRemove(arg, defKey).let {
                val result = parse(it.first, replace, textParamBean)
                return if (result.isEmpty()) {
                    it.second
                } else {
                    result
                }
            }
        }
    }

    /**解析文本
     * $0 从[com.angcyo.acc2.bean.TaskBean.wordList] 取第一个
     * $-2 从[com.angcyo.acc2.bean.TaskBean.wordList] 取倒数第二个
     * $0~$-2 取范围内的字符
     * $[xxx] 从[com.angcyo.acc2.bean.TaskBean.textMap]获取[xxx]键值对应的值
     *
     * [replace] 是否只是替换掉原来的字符
     * */
    fun parse(
        arg: String?,
        replace: Boolean = false,
        textParamBean: TextParamBean? = null
    ): List<String?> {
        if (arg.isNullOrEmpty()) {
            return emptyList()
        }
        if (!haveVarFlag(arg)) {
            return listOf(arg)
        }

        val result = mutableListOf<String?>()

        var isHandle = false

        val taskBean = accControl._taskBean
        val wordList: List<String?>

        //替换后的字符串
        var replaceResult = arg

        //$[xxx], 在map中获取文本
        val mapKeyList = parseTextKey(arg)
        if (mapKeyList.isNotEmpty()) {
            isHandle = true
            val keyResult = mutableListOf<String?>()
            mapKeyList.forEach { key ->
                when (key) {
                    Action.APP_NAME -> {
                        val appName =
                            parsePackageName(null, accControl._taskBean?.packageName).firstOrNull()
                                ?.appBean()?.appName?.str()
                        if (appName.isNullOrBlank()) {
                            taskBean?.getTextList(key)?.firstOrNull()?.let { value ->
                                keyResult.add(value)
                                if (replace) {
                                    replaceResult = replaceResult?.replace("$[$key]", value)
                                }
                            }
                        } else {
                            //程序名
                            keyResult.add(appName)
                            if (replace) {
                                replaceResult = replaceResult?.replace("$[$key]", appName)
                            }
                        }
                    }
                    Action.NOW_TIME -> {
                        val text = nowTimeString()
                        keyResult.add(text)
                        if (replace) {
                            replaceResult = replaceResult?.replace("$[$key]", text)
                        }
                    }
                    Action.LAST_INPUT -> {
                        val text = accControl.accSchedule.inputTextList.lastOrNull()
                        if (text != null) {
                            keyResult.add(text)
                            if (replace) {
                                replaceResult = replaceResult?.replace("$[$key]", text)
                            }
                        }
                    }
                    else -> {
                        getTextOfListMap(key)?.apply {
                            keyResult.addAll(this)
                            if (replace) {
                                replaceResult = replaceResult?.replace("$[$key]", this.str())
                            }
                        } ?: getTextOfMap(key)?.let { value ->
                            keyResult.add(value)
                            if (replace) {
                                replaceResult = replaceResult?.replace("$[$key]", value)
                            }
                        }
                    }
                }
            }

            //如果指定了$[xxx]
            wordList = keyResult.toList()
            result.addAll(keyResult)
        } else {
            wordList = taskBean?.wordList ?: emptyList()
        }

        //$0~$-2
        val indexStringList = arg.patternList("\\$[-]?\\d+")
        if (indexStringList.isNotEmpty()) {
            //$xxx 的情况
            if (arg.havePartition()) {
                //$0~$1
                if (indexStringList.size >= 2) {
                    isHandle = true
                    val startIndex = indexStringList[0].getLongNum()?.revise(wordList.size) ?: 0
                    val endIndex = indexStringList[1].getLongNum()?.revise(wordList.size) ?: 0

                    val rangeWordList = mutableListOf<String?>()
                    wordList.forEachIndexed { index, word ->
                        if (index in startIndex..endIndex) {
                            result.add(word)
                            rangeWordList.add(word)
                        }
                    }
                    if (replace) {
                        replaceResult = replaceResult?.replace(
                            "$$startIndex${Action.POINT_SPLIT}$${endIndex}",
                            rangeWordList.str()
                        )
                    }
                } else {
                    isHandle = true
                    indexStringList.forEach { indexString ->
                        indexString.getLongNum()?.let { index ->
                            wordList.getOrNull(index.toInt())?.let { word ->
                                result.add(word)

                                if (replace) {
                                    replaceResult = replaceResult?.replace("$$index", word)
                                }
                            }
                        }
                    }
                }
            } else {
                //$0 $2 $3
                isHandle = true
                indexStringList.forEach { indexString ->
                    indexString.getLongNum()?.let { index ->
                        wordList.getOrNull(index.toInt())?.let { word ->
                            result.add(word)

                            if (replace) {
                                replaceResult = replaceResult?.replace("$$index", word)
                            }
                        }
                    }
                }
            }
        }

        if (!isHandle) {
            //都未处理
            result.add(arg)
        }

        if (result.isEmpty() || (replace && arg == replaceResult)) {
            accControl.log("无法解析文本参数[$arg]↓\n${taskBean?.wordList}\n${taskBean?.textMap}\n${taskBean?.textListMap}")
        }

        if (replace) {
            accControl.log("文本替换后[$arg]->[$replaceResult]")
            return parseTextParam(listOf(replaceResult), textParamBean)
        }

        return parseTextParam(result, textParamBean)
    }

    /**是否有变量标识符*/
    fun haveVarFlag(arg: String?) = arg?.contains("$") == true

    /**替换长尾词*/
    fun parseTextParam(originList: List<String?>, textParamBean: TextParamBean?): List<String?> {
        return if (textParamBean != null && !textParamBean.tailList.isNullOrEmpty()) {
            val result = mutableListOf<String?>()
            originList.forEach { text ->
                if (text.isNullOrEmpty()) {
                    result.add(text)
                } else {
                    result.add(handleText(text, textParamBean))
                }
            }
            accControl.log("长尾词替换后$originList->$result")
            result
        } else {
            originList
        }
    }

    /**[xx:$[xxx] $[xxx] xxx]获取$[xxx]格式中的xxx*/
    fun parseTextKey(arg: String) = arg.patternList("(?<=\\$\\[)[\\s\\S]*?(?=\\])")

    /**[xx:$[xxx] $[xxx] xxx]从中获取指定的key对应的值, 并且抹掉后返回
     * [first] 移除后的文本
     * [second] key对应的值*/
    fun parseTextKeyAndRemove(arg: String, key: String): Pair<String, List<String?>> {
        val value = arg.arg(key)
        val valueResult = parse(value)

        //替换后的字符串
        val replaceResult = arg.replace("$key:$[$value]", "").trim()

        return replaceResult to valueResult
    }

    /**单个文本替换长尾词*/
    fun handleText(text: String?, textParamBean: TextParamBean?): String? {
        var result = text
        if (result.isNullOrEmpty() || textParamBean == null) {
            return result
        }
        textParamBean.tailList?.forEachIndexed { index, tail ->
            if (!tail.isNullOrEmpty()) {
                //需要替换的词
                val upList = textParamBean.tailUpList?.getOrNull(index) //替换词列表
                if (!upList.isNullOrEmpty()) {
                    result = result?.replace(tail, upList.randomGet(1).first())
                }
            }
        }
        return result
    }

    /**将参数转换成对应的包名*/
    fun parsePackageName(
        arg: String? = null,
        target: String? = findParse.windowBean()?.packageName ?: accControl._taskBean?.packageName
    ): List<String> {
        val result = mutableListOf<String>()

        val nameArg = arg ?: target
        nameArg?.split(Action.PACKAGE_SPLIT)?.forEach { name ->
            val packageName = if (name.isEmpty()) {
                target
            } else if (name == Action.PACKAGE_MAIN) {
                app().packageName
            } else if (name == Action.PACKAGE_TARGET) {
                if (target.isNullOrEmpty()) {
                    //优先使用task的包名, 确保不是空
                    accControl._taskBean?.packageName?.split(Action.PACKAGE_SPLIT)?.firstOrNull()
                        ?: target
                } else {
                    target
                }
            } else if (name == Action.PACKAGE_ACTIVE) {
                accControl.accService()?.rootInActiveWindow?.packageName
            } else {
                name
            }?.toStr()

            if (!packageName.isNullOrBlank()) {
                result.add(packageName)
            }
        }

        return result
    }

    fun getTextOfMap(key: String) = accControl._taskBean?.textMap?.get(key)

    fun getTextOfListMap(key: String) = accControl._taskBean?.textListMap?.get(key)
}
