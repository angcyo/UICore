/*
 *  Copyright (C)  2016 android@19code.com
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.angcyo.library

import android.text.TextUtils
import android.util.Log
import com.angcyo.library.ex.isDebug
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.StringReader
import java.io.StringWriter
import javax.xml.transform.OutputKeys
import javax.xml.transform.Source
import javax.xml.transform.TransformerFactory
import javax.xml.transform.stream.StreamResult
import javax.xml.transform.stream.StreamSource

/**
 * Created by Gh0st on 2016/6/7 007.
 * https://github.com/ZhaoKaiQiang/KLog
 */
object L {
    private val LINE_SEPARATOR = System.getProperty("line.separator")
    private const val VERBOSE = 2
    private const val DEBUG = 3
    private const val INFO = 4
    private const val WARN = 5
    private const val ERROR = 6
    private const val ASSERT = 7
    private const val JSON = 8
    private const val XML = 9
    private const val JSON_INDENT = 4
    var TAG = "angcyo"
    var LOG_DEBUG = BuildConfig.DEBUG
    private var lastLogETime = 0L
    private var lastLogITime = 0L

    fun init(tag: String, debug: Boolean = isDebug()) {
        TAG = tag
        LOG_DEBUG = debug
    }

    fun v(msg: String) {
        log(VERBOSE, null, msg)
    }

    fun v(tag: String, msg: String) {
        log(VERBOSE, tag, msg)
    }

    fun d(msg: String) {
        log(DEBUG, null, msg)
    }

    fun d(tag: String, msg: String) {
        log(DEBUG, tag, msg)
    }

    fun i(vararg msg: Any) {
        val sb = StringBuilder()
        for (obj in msg) {
            sb.append("$obj,")
        }
        log(INFO, null, sb.substring(0, Math.max(0, sb.length - 1)))
    }

    fun i2(interval: Long /*毫秒*/, vararg msg: Any) {
        val millis = System.currentTimeMillis()
        if (millis - lastLogITime > interval) {
            i(*msg)
            lastLogITime = millis
        }
    }

    fun w(msg: String) {
        log(WARN, null, msg)
    }

    fun w(tag: String, msg: String) {
        log(WARN, tag, msg)
    }

    fun e(msg: String) {
        log(ERROR, null, msg)
    }

    /**
     * @param interval 控制log输出的速度
     */
    @JvmOverloads
    fun e(
        msg: String,
        interval: Long /*毫秒*/,
        ignore: Boolean = false /*忽略本次*/
    ) {
        val millis = System.currentTimeMillis()
        if (ignore || millis - lastLogETime > interval) {
            log(ERROR, null, msg)
            lastLogETime = millis
        }
    }

    fun e(tag: String?, msg: String) {
        log(ERROR, tag, msg)
    }

    fun a(msg: String) {
        log(ASSERT, null, msg)
    }

    fun a(tag: String?, msg: String) {
        log(ASSERT, tag, msg)
    }

    fun json(json: String) {
        log(JSON, null, json)
    }

    fun json(tag: String?, json: String) {
        log(JSON, tag, json)
    }

    fun xml(xml: String) {
        log(XML, null, xml)
    }

    fun xml(tag: String?, xml: String) {
        log(XML, tag, xml)
    }

    private fun log(logType: Int, tagStr: String?, objects: Any) {
        val contents = wrapperContent(tagStr, objects)
        val tag = contents[0]
        val msg = contents[1]
        val headString = contents[2]
        if (LOG_DEBUG) {
            when (logType) {
                VERBOSE, DEBUG, INFO, WARN, ERROR, ASSERT -> printDefault(
                    logType,
                    tag,
                    headString + msg
                )
                JSON -> printJson(tag, msg, headString)
                XML -> printXml(tag, msg, headString)
                else -> {
                }
            }
        }
    }

    fun printDefault(type: Int, tag: String?, msg: String) {
        var tag = tag
        if (TextUtils.isEmpty(tag)) {
            tag = TAG
        }
        var index = 0
        val maxLength = 4000
        val countOfSub = msg.length / maxLength
        if (countOfSub > 0) { // The log is so long
            for (i in 0 until countOfSub) {
                val sub = msg.substring(index, index + maxLength)
                printSub(type, tag, sub)
                index += maxLength
            }
            //printSub(type, msg.substring(index, msg.length()));
        } else {
            printSub(type, tag, msg)
        }
    }

    private fun printSub(type: Int, tag: String?, sub: String) {
        var tag = tag
        if (tag == null) {
            tag = TAG
        }
        when (type) {
            VERBOSE -> Log.v(tag, sub)
            DEBUG -> Log.d(tag, sub)
            INFO -> Log.i(tag, sub)
            WARN -> Log.w(tag, sub)
            ERROR -> Log.e(tag, sub)
            ASSERT -> Log.wtf(tag, sub)
        }
    }

    private fun printJson(
        tag: String?,
        json: String?,
        headString: String?
    ) {
        var tag = tag
        if (TextUtils.isEmpty(json)) {
            d("Empty/Null json content")
            return
        }
        if (TextUtils.isEmpty(tag)) {
            tag = TAG
        }
        var message: String?
        message = try {
            if (json!!.startsWith("{")) {
                val jsonObject = JSONObject(json)
                jsonObject.toString(JSON_INDENT)
            } else if (json.startsWith("[")) {
                val jsonArray = JSONArray(json)
                jsonArray.toString(JSON_INDENT)
            } else {
                json
            }
        } catch (e: JSONException) {
            json
        }
        printLine(tag, true)
        message = headString + LINE_SEPARATOR + message
        val lines = message.split(LINE_SEPARATOR!!).toTypedArray()
        for (line in lines) {
            Log.d(tag, "|$line")
        }
        printLine(tag, false)
    }

    private fun printXml(
        tag: String?,
        xml: String?,
        headString: String?
    ) {
        var tag = tag
        var xml = xml
        if (TextUtils.isEmpty(tag)) {
            tag = TAG
        }
        if (xml != null) {
            try {
                val xmlInput: Source =
                    StreamSource(StringReader(xml))
                val xmlOutput =
                    StreamResult(StringWriter())
                val transformer =
                    TransformerFactory.newInstance().newTransformer()
                transformer.setOutputProperty(OutputKeys.INDENT, "yes")
                transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2")
                transformer.transform(xmlInput, xmlOutput)
                xml = xmlOutput.writer.toString().replaceFirst(">".toRegex(), ">\n")
            } catch (e: Exception) {
                e.printStackTrace()
            }
            xml = headString + "\n" + xml
        } else {
            xml = headString + "Log with null object"
        }
        printLine(tag, true)
        val lines = xml.split(LINE_SEPARATOR!!).toTypedArray()
        for (line in lines) {
            if (!TextUtils.isEmpty(line)) {
                Log.d(tag, "|$line")
            }
        }
        printLine(tag, false)
    }

    fun wrapperContent(tag: String?, vararg objects: Any): Array<String> {
        return wrapperContent(6, tag, *objects)
    }

    fun wrapperContent(level: Int, tag: String?, vararg objects: Any): Array<String> {
        var tag = tag
        if (TextUtils.isEmpty(tag)) {
            tag = TAG
        }
        val stackTrace =
            Thread.currentThread().stackTrace
        val targetElement = stackTrace[level]
        //        String className = targetElement.getClassName();
        val fileName = targetElement.fileName
        //        String[] classNameInfo = className.split("\\.");
//        if (classNameInfo.length > 0) {
//            className = classNameInfo[classNameInfo.length - 1] + ".java";
//        }
        val methodName = targetElement.methodName
        var lineNumber = targetElement.lineNumber
        if (lineNumber < 0) {
            lineNumber = 0
        }
        val nextFileName: String
        val nextMethodName: String
        var nextLineNumber: Int
        if (level + 1 == stackTrace.size) { //到顶了
            nextFileName = "Top"
            nextMethodName = "Top"
            nextLineNumber = -1
        } else {
            val next = stackTrace[level + 1]
            nextFileName = next.fileName
            nextMethodName = next.methodName
            nextLineNumber = next.lineNumber
            if (nextLineNumber < 0) {
                nextLineNumber = 0
            }
        }
        val methodNameShort =
            methodName.substring(0, 1).toUpperCase() + methodName.substring(1)
        val msg =
            objects.let { getObjectsString(*it) }
        //        String headString = "[(" + className + ":" + lineNumber + ")#" + methodNameShort + " ] ";
        val headString =
            "[(" + nextFileName + ":" + nextLineNumber + ")#" + nextMethodName +
                    "(" + fileName + ":" + lineNumber + ")#" + Thread.currentThread().name + "#" + methodNameShort + " ] "
        return arrayOf(tag!!, msg, headString)
    }

    private fun getObjectsString(vararg objects: Any): String {
        return if (objects.size > 1) {
            val stringBuilder = StringBuilder()
            stringBuilder.append("\n")
            for (i in objects.indices) {
                val obj = objects[i]
                stringBuilder.append("param").append("[").append(i).append("]").append(" = ")
                    .append(obj.toString()).append("\n")
            }
            stringBuilder.toString()
        } else {
            val obj = objects[0]
            obj.toString()
        }
    }

    fun printLine(tag: String?, isTop: Boolean) {
        if (isTop) {
            Log.d(
                tag,
                "╔═══════════════════════════════════════════════════════════════════════════════════════"
            )
        } else {
            Log.d(
                tag,
                "╚═══════════════════════════════════════════════════════════════════════════════════════"
            )
        }
    }
}