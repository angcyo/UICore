package com.angcyo.library

import android.util.Log
import com.angcyo.library.ex.isShowDebug
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.max

/**
 * 日志输出类
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/12/30
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */

typealias LogPrint = (tag: String, level: Int, msg: String) -> Unit

object L {

    val LINE_SEPARATOR = System.getProperty("line.separator")

    /**数据结构item分割*/
    val ARRAY_SEPARATOR = ","

    /**多log消息输出分割*/
    val LOG_SEPARATOR = " "

    const val NONE = 0
    const val VERBOSE = 2
    const val DEBUG = 3
    const val INFO = 4
    const val WARN = 5
    const val ERROR = 6
    const val FILE = 0xff//只写入文件, 不输出控制台

    val DEFAULT_LOG_PRINT: LogPrint = { tag, level, msg ->
        when (level) {
            VERBOSE -> Log.v(tag, msg)
            DEBUG -> Log.d(tag, msg)
            INFO -> Log.i(tag, msg)
            WARN -> Log.w(tag, msg)
            ERROR -> Log.e(tag, msg)
        }
    }

    var debug = isShowDebug()

    var tag: String = "L"
        get() {
            return _tempTag ?: field
        }

    /**打印多少级的堆栈信息*/
    var stackTraceDepth: Int = 2
        get() = if (_tempStackTraceDepth > 0) _tempStackTraceDepth else field

    var _tempStackTraceDepth: Int = -1

    /**堆栈跳过前多少个*/
    var stackTraceFront: Int = 2
        get() = if (_tempStackTraceFront > 0) _tempStackTraceFront else field

    var _tempStackTraceFront: Int = -1

    /**Json缩进偏移量*/
    var indentJsonDepth: Int = 2

    /**日志输出列表*/
    val logPrintList = mutableListOf<LogPrint>()

    /**打印回调*/
    var logPrint: LogPrint = DEFAULT_LOG_PRINT

    //临时tag
    var _tempTag: String? = null

    /**>=此级别的日志, 才输出到文件*/
    var fileLevel: Int = INFO

    //当前日志输出级别
    var _level: Int = DEBUG

    fun init(tag: String, debug: Boolean = isShowDebug()) {
        this.tag = tag
        this.debug = debug
    }

    fun v(vararg msg: Any?) {
        _level = VERBOSE
        _log(*msg)
    }

    fun d(vararg msg: Any?) {
        _level = DEBUG
        _log(*msg)
    }

    fun i(vararg msg: Any?) {
        _level = INFO
        _log(*msg)
    }

    fun w(vararg msg: Any?) {
        _level = WARN
        _log(*msg)
    }

    fun e(vararg msg: Any?) {
        _level = ERROR
        _log(*msg)
    }

    fun f(vararg msg: Any?) {
        _level = FILE
        _log(*msg)
    }

    fun vt(tag: String, vararg msg: Any?) {
        _tempTag = tag
        _level = VERBOSE
        _log(*msg)
    }

    fun dt(tag: String, vararg msg: Any?) {
        _tempTag = tag
        _level = DEBUG
        _log(*msg)
    }

    fun it(tag: String, vararg msg: Any?) {
        _tempTag = tag
        _level = INFO
        _log(*msg)
    }

    fun wt(tag: String, vararg msg: Any?) {
        _tempTag = tag
        _level = WARN
        _log(*msg)
    }

    fun et(tag: String, vararg msg: Any?) {
        _tempTag = tag
        _level = ERROR
        _log(*msg)
    }

    fun log(level: Int, vararg msg: Any?) {
        _level = level
        _log(*msg)
    }

    fun _log(vararg msg: Any?) {
        if (!_needParseLog()) {
            return
        }
        if (!debug && _level < fileLevel) {
            //非文件log
            return
        }

        //调用栈信息
        val stackBuilder = StringBuilder()
        val stackTrace = getStackTrace(stackTraceFront, stackTraceDepth)
        val stackContext = stackBuilder.apply {
            append("[")
            stackTrace.forEachIndexed { index, element ->
                append("(")
                append(element.fileName)
                if (index == stackTrace.lastIndex) {
                    append(":")
                    append(element.lineNumber)
                    append(")")
                }
                append("#")
                append(element.methodName)

                if (index == stackTrace.lastIndex) {
                    append(":")
                    append(Thread.currentThread().name)
                } else {
                    append("#")
                    append(element.lineNumber)
                    append(" ")
                }
            }
            append("]")
        }

        //log内容
        val logBuilder = StringBuilder()
        val logMsg = logBuilder.apply {
            msg.forEachIndexed { msgIndex, msgAny ->
                when (msgAny) {
                    is CharSequence -> append(_wrapJson("$msgAny"))
                    is Iterable<*> -> {
                        append("[")
                        msgAny.forEachIndexed { index, any ->
                            append(any.toString())
                            if (index != msgAny.count() - 1) {
                                append(ARRAY_SEPARATOR)
                            }
                        }
                        append("]")
                    }

                    is Array<*> -> {
                        append("[")
                        msgAny.forEachIndexed { index, any ->
                            append(any.toString())
                            if (index != msgAny.count() - 1) {
                                append(ARRAY_SEPARATOR)
                            }
                        }
                        append("]")
                    }

                    is IntArray -> {
                        append("[")
                        msgAny.forEachIndexed { index, any ->
                            append(any.toString())
                            if (index != msgAny.count() - 1) {
                                append(ARRAY_SEPARATOR)
                            }
                        }
                        append("]")
                    }

                    is LongArray -> {
                        append("[")
                        msgAny.forEachIndexed { index, any ->
                            append(any.toString())
                            if (index != msgAny.count() - 1) {
                                append(ARRAY_SEPARATOR)
                            }
                        }
                        append("]")
                    }

                    is FloatArray -> {
                        append("[")
                        msgAny.forEachIndexed { index, any ->
                            append(any.toString())
                            if (index != msgAny.count() - 1) {
                                append(ARRAY_SEPARATOR)
                            }
                        }
                        append("]")
                    }

                    is DoubleArray -> {
                        append("[")
                        msgAny.forEachIndexed { index, any ->
                            append(any.toString())
                            if (index != msgAny.count() - 1) {
                                append(ARRAY_SEPARATOR)
                            }
                        }
                        append("]")
                    }

                    else -> append(msgAny.toString())
                }
                if (msgIndex != msg.count() - 1) {
                    append(LOG_SEPARATOR)
                }
            }
        }

        try {
            //list
            logPrintList.forEach {
                it(tag, _level, "$stackContext $logMsg")
            }

            if (debug || _level >= fileLevel) {
                //debug模式下, 获取时File日志
                logPrint(tag, _level, "$stackContext $logMsg")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        _tempTag = null
        _tempStackTraceDepth = -1
        _tempStackTraceFront = -1
    }

    fun _wrapJson(msg: String): String {
        if (indentJsonDepth <= 0) {
            return msg
        }
        try {
            if (msg.startsWith("{") && msg.endsWith("}")) {
                val jsonObject = JSONObject(msg)
                return LINE_SEPARATOR + jsonObject.toString(indentJsonDepth)
            } else if (msg.startsWith("[") && msg.endsWith("]")) {
                val jsonArray = JSONArray(msg)
                return LINE_SEPARATOR + jsonArray.toString(indentJsonDepth)
            }
        } catch (e: Exception) {

        }
        return msg
    }

    /**是否需要解析日志*/
    fun _needParseLog(): Boolean {
        if (logPrintList.isNotEmpty()) {
            return true
        }
        if (!debug && _level < fileLevel) {
            //非文件log
            return false
        }
        return true
    }
}

/**
 * 获取调用栈信息
 * [front] 当前调用位置的前几个开始
 * [count] 共几个, 负数表示全部
 * */
fun getStackTrace(front: Int = 0, count: Int = -1): List<StackTraceElement> {
    val stackTrace = Thread.currentThread().stackTrace
    stackTrace.reverse()
    val endIndex = max(0, stackTrace.size - 3 - front)
    val startIndex = if (count > 0) max(0, endIndex - count) else 0
    if (endIndex == 0) {
        return emptyList()
    }
    val slice = stackTrace.slice(startIndex until endIndex)
    return slice
}

/**调用栈信息, 降序排序*/
fun stackTraceString() = getStackTrace(1).joinToString("\n")