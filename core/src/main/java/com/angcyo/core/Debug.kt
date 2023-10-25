package com.angcyo.core

import android.widget.EditText
import androidx.fragment.app.FragmentActivity
import com.angcyo.base.dslFHelper
import com.angcyo.core.CoreApplication.Companion.DEFAULT_FILE_PRINT_PATH
import com.angcyo.core.component.DslCrashHandler
import com.angcyo.core.component.fileSelector
import com.angcyo.library.Library
import com.angcyo.library.component.lastContext
import com.angcyo.library.ex.Action
import com.angcyo.library.ex.file
import com.angcyo.library.ex.hawkDelete
import com.angcyo.library.ex.hawkGet
import com.angcyo.library.ex.hawkPut
import com.angcyo.library.ex.longFeedback
import com.angcyo.library.ex.shareFile
import com.angcyo.library.toastQQ
import com.angcyo.library.utils.Constant
import com.angcyo.library.utils.FileUtils
import com.angcyo.library.utils.appFolderPath
import com.angcyo.library.utils.logFileName

/**
 * 输入框调试模式指令
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2021/09/30
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */

/**返回是否要拦截默认处理*/
typealias DebugCommandAction = (Debug.DebugCommand) -> Boolean

object Debug {

    /**开启调试模式*/
    var onChangedToDebug: MutableList<Action> = mutableListOf()

    /**显示界面回调*/
    var onShowFragmentAction: ((FragmentActivity, show: String) -> Unit)? = null

    /**回调*/
    var debugCommandActionList = mutableListOf<DebugCommandAction>()

    /**调试:文本输入框文本改变时
     * [com.angcyo.widget.edit.BaseEditDelegate.Companion.textChangedActionList]*/
    fun onDebugTextChanged(
        editText: EditText?,
        text: CharSequence?,
        start: Int,
        lengthBefore: Int,
        lengthAfter: Int
    ) {
        val inputText = text?.toString()
        if (inputText.isNullOrBlank()) {
            return
        }
        var match = false
        when (inputText.lowercase()) {
            //开启调试模式
            "@cmd#debug", "@9.999999" -> {
                Library.isDebugTypeVal = true
                onChangedToDebug.forEach {
                    it()
                }
                match = true
            }
            //分享http日志文件
            "@cmd#share=http", "@9.777777" -> {
                val file = FileUtils.appRootExternalFolderFile(
                    Constant.HTTP_FOLDER_NAME,
                    logFileName()
                )
                file.shareFile()
                match = true
            }
            //分享L.log
            "@cmd#share=l", "@9.111111" -> {
                val file = DEFAULT_FILE_PRINT_PATH?.file()
                file?.shareFile()
                match = true
            }
            //分享crash.log
            "@cmd#share=crash", "@9.333333" -> {
                val file = DslCrashHandler.KEY_CRASH_FILE.hawkGet()?.file()
                file?.shareFile()
                match = true
            }

            "@cmd#open=file", "@9.555555" -> {
                //打开文件预览对话框
                if (editText?.context is FragmentActivity) {
                    editText.context
                } else {
                    lastContext
                }?.apply {
                    if (this is FragmentActivity) {
                        dslFHelper {
                            fileSelector({
                                showFileMd5 = true
                                showFileMenu = true
                                showHideFile = true
                                targetPath = appFolderPath()
                            }) {
                                //no op
                            }
                        }
                        editText?._feedback()
                    }
                }
            }

            else -> {
                //@key#int=value 此指令用来设置hawk key value
                parseHawkKeys(inputText.lines(), editText)
            }
        }
        if (match) {
            editText?._feedback()
        }
    }

    /**解析 @key#int=value 此指令用来设置hawk key value */
    fun parseHawkKeys(lines: List<String?>?, editText: EditText? = null) {
        var match = false
        try {
            lines?.forEach { line ->
                if (line == null) {
                    //no op
                } else {
                    val keyIndex = line.indexOf("@")
                    val typeIndex = line.indexOf("#")
                    val valueIndex = line.indexOf("=")
                    if (keyIndex != -1 && typeIndex != -1 && valueIndex != -1) {

                        //@key#int=value
                        val key = line.substring(keyIndex + 1, typeIndex)
                        val type = line.substring(typeIndex + 1, valueIndex)
                        val valueString = line.substring(valueIndex + 1, line.length)

                        var intercept = false
                        try {
                            for (action in debugCommandActionList) {
                                intercept = intercept || action.invoke(
                                    DebugCommand(
                                        line,
                                        key,
                                        type,
                                        valueString
                                    )
                                )
                            }
                            if (intercept) {
                                match = true
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                        if (!intercept && key.isNotBlank()) {
                            when (key.lowercase()) {
                                "cmd" -> {
                                    when (type) {
                                        "open" -> {
                                            //打开Fragment界面
                                            //@cmd#open=value
                                            lastContext.apply {
                                                if (this is FragmentActivity) {
                                                    onShowFragmentAction?.invoke(this, valueString)
                                                    match = true
                                                }
                                            }
                                        }

                                        "clear" -> {
                                            //删除hawk的键
                                            //@cmd#clear=key
                                            valueString.hawkDelete()
                                            match = true
                                        }

                                        "show", "hawk" -> {
                                            //显示hawk的值
                                            //@cmd#hawk=key
                                            //@cmd#show=key
                                            toastQQ("${valueString.hawkGet<Any>()}")
                                            match = true
                                        }
                                    }
                                }

                                else -> {
                                    //@key#int=value
                                    when (type) {
                                        "b", "bool", "boolean" -> {
                                            val value = valueString.toBoolean()
                                            key.hawkPut(value)
                                            match = true
                                        }

                                        "int", "i" -> {
                                            val value = valueString.toIntOrNull()
                                            if (value == null) {
                                                key.hawkDelete()
                                            } else {
                                                key.hawkPut(value)
                                            }
                                            match = true
                                        }

                                        "long", "l" -> {
                                            val value = valueString.toLongOrNull()
                                            if (value == null) {
                                                key.hawkDelete()
                                            } else {
                                                key.hawkPut(value)
                                            }
                                            match = true
                                        }

                                        "float", "f" -> {
                                            val value = valueString.toFloatOrNull()
                                            if (value == null) {
                                                key.hawkDelete()
                                            } else {
                                                key.hawkPut(value)
                                            }
                                            match = true
                                        }

                                        "double", "d" -> {
                                            val value = valueString.toDoubleOrNull()
                                            if (value == null) {
                                                key.hawkDelete()
                                            } else {
                                                key.hawkPut(value)
                                            }
                                            match = true
                                        }

                                        "string", "s" -> {
                                            key.hawkPut(valueString)
                                            match = true
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (match) {
            editText?._feedback()
        }
    }

    /**反馈*/
    private fun EditText._feedback() {
        selectAll()
        longFeedback()
    }

    /**调试指令信息
     * ```
     * @key#int=value
     * ```*/
    data class DebugCommand(
        val command: String,
        val key: String,
        val type: String,
        val value: String,
    )
}