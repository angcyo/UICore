package com.angcyo.library

import android.util.Log
import com.angcyo.library.ex.isDebug
import org.json.JSONArray
import org.json.JSONObject

/**
 * 日志输出类
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/12/30
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */

object L {
    val LINE_SEPARATOR = System.getProperty("line.separator")

    const val VERBOSE = 2
    const val DEBUG = 3
    const val INFO = 4
    const val WARN = 5
    const val ERROR = 6

    var debug = isDebug()

    var tag: String = "L"
        get() {
            return _tempTag ?: field
        }

    /**打印多少级的堆栈信息*/
    var stackTraceDepth: Int = 2

    /**Json缩进偏移量*/
    var indentJsonDepth: Int = 2

    //临时tag
    var _tempTag: String? = null

    //当前日志输出级别
    var _level: Int = DEBUG

    fun init(tag: String, debug: Boolean = isDebug()) {
        this.tag = tag
        this.debug = debug
    }

    fun v(msg: Any?) {
        _level = VERBOSE
        _log(msg)
    }

    fun d(msg: Any?) {
        _level = DEBUG
        _log(msg)
    }

    fun i(msg: Any?) {
        _level = INFO
        _log(msg)
    }

    fun w(msg: Any?) {
        _level = WARN
        _log(msg)
    }

    fun e(msg: Any?) {
        _level = ERROR
        _log(msg)
    }

    fun v(tag: String, msg: Any?) {
        _tempTag = tag
        _level = VERBOSE
        _log(msg)
    }

    fun d(tag: String, msg: Any?) {
        _tempTag = tag
        _level = DEBUG
        _log(msg)
    }

    fun i(tag: String, msg: Any?) {
        _tempTag = tag
        _level = INFO
        _log(msg)
    }

    fun w(tag: String, msg: Any?) {
        _tempTag = tag
        _level = WARN
        _log(msg)
    }

    fun e(tag: String, msg: Any?) {
        _tempTag = tag
        _level = ERROR
        _log(msg)
    }

    fun _log(msg: Any?) {
        if (!debug) {
            return
        }

        val stackTrace = getStackTrace(2, stackTraceDepth)
        val stackContext = buildString {
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

        val logMsg = if (msg is CharSequence) {
            _wrapJson("$msg")
        } else {
            msg
        }

        when (_level) {
            VERBOSE -> Log.v(tag, "$stackContext $logMsg")
            DEBUG -> Log.d(tag, "$stackContext $logMsg")
            INFO -> Log.i(tag, "$stackContext $logMsg")
            WARN -> Log.w(tag, "$stackContext $logMsg")
            ERROR -> Log.e(tag, "$stackContext $logMsg")
        }

        _tempTag = null
    }

    fun _wrapJson(msg: String): String {
        if (indentJsonDepth <= 0) {
            return msg
        }
        try {
            if (msg.startsWith("{")) {
                val jsonObject = JSONObject(msg)
                return LINE_SEPARATOR + jsonObject.toString(indentJsonDepth)
            } else if (msg.startsWith("[")) {
                val jsonArray = JSONArray(msg)
                return LINE_SEPARATOR + jsonArray.toString(indentJsonDepth)
            }
        } catch (e: Exception) {

        }
        return msg
    }
}

/**
 * 获取调用栈信息
 * [front] 当前调用位置的前几个开始
 * [count] 共几个
 * */
fun getStackTrace(front: Int = 0, count: Int = -1): List<StackTraceElement> {
    val stackTrace = Thread.currentThread().stackTrace
    stackTrace.reverse()
    val endIndex = stackTrace.size - 3 - front
    val startIndex = if (count > 0) (endIndex - count) else 0
    val slice = stackTrace.slice(startIndex until endIndex)
    return slice
}
